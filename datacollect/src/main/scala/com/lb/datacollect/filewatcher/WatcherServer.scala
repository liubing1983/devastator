package com.lb.datacollect.filewatcher

import java.io.File
import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW}
import java.nio.file.WatchEvent.Kind
import java.nio.file.{WatchEvent, _}

import org.apache.log4j.Logger;

import akka.actor.{Terminated, Actor, ActorSystem, Props}
import com.lb.utils.{DateUtils, FileUtils, FromFilePath, Metadata}

import scala.collection.JavaConversions._

case class WatcherServerBean(val watcher_path: String,
                             val dest_path: String,
                             // 目的目录分区 ０－不分区 ２４－天 ６０－小时 ５／１０／１５／２０／３０－＿分钟， 与dest_files成对出现
                             val partitions: String,
                             // 文件备份目录
                             val back_path: String,
                             // 错误文件目录
                             val error_path: String,
                             // 监控文件后缀
                             val suffix: String,
                             // 效验文件后缀
                             val check_suffix: String)

/**
 * Created by liubing on 16-10-10.
 */
class WatcherServer() extends Actor {

 // val logger: Logger = LoggerFactory.getLogger("WatcherServer")

  val logger : Logger  = Logger.getLogger(WatcherServer.getClass)

  logger.info("hhhshshshshshshshshshshshshshsh")

  val putActor = context.actorOf(Props[FileToHdfsActor], name = s"putActor")

  // 监控actor的状态
  context.watch(putActor)

  def receive = {
    case WatcherServerBean(watcher_path, hdfs_path, partitions, back_path, error_path, suffix, check_suffix) => {


      // 计算分区方式
      val partinion = partitionData(partitions)

      // 启动监控前查看文件夹内是否存在文件
      // 得到监控目录中的校验文件列表
      FileUtils.getListOfFiles(new File(watcher_path), List(check_suffix)).foreach { filename =>
        // 根据得到的文件列表上传对应的数据文件
        putActor ! FileToHdfsActorBean(filename.getPath.replace(".ok", suffix), filename.getPath, s"${hdfs_path}/${partinion}/", s"$back_path/$partinion/", s"$error_path/$partinion/")
      }

      // 得到监控目录中所有剩余文件/文件夹列表
      FileUtils.getListOfFiles(new File(watcher_path)).foreach(org.apache.commons.io.FileUtils.moveDirectory(_, new File(s"$error_path/$partinion/")))


      val watcher: WatchService = FileSystems.getDefault().newWatchService()
      // 监控目录内文件的更新、创建和删除事件
      val path = Paths.get(watcher_path)
      // ENTRY_CREATE: 注册创建任务
      val key: WatchKey = path.register(watcher, ENTRY_CREATE)

      logger.info(s"Thread name: ${Thread.currentThread().getName()}, 监控目录:  $watcher_path , 目的目录: $hdfs_path")

      while (true) {
        // 等待直到获得事件信号
        var signal: WatchKey = null

        try {
          signal = watcher.take()
        } catch {
          case x: InterruptedException =>
        }

        signal.pollEvents.toList.foreach { event2 =>
          val event = event2.asInstanceOf[WatchEvent[Path]]

          val kind: Kind[Path] = event.kind(): Kind[Path]

          if (kind == OVERFLOW) {

          }

          val name: Path = cast(event).context()

          (kind, name) match {
            case (ENTRY_CREATE, name) if (name.toString().endsWith(check_suffix)) =>
              logger.debug(s"check_suffix, 监控目录发现新校验文件, ${name.toString}, ${System.currentTimeMillis()}")
              putActor ! FileToHdfsActorBean(s"${watcher_path}/${name.toString.replace(check_suffix, suffix)}", s"${watcher_path}/${name.toString}", s"${hdfs_path}/${partinion}/", s"$back_path/$partinion/", s"$error_path/$partinion/")
            case (ENTRY_CREATE, name) if (name.toString().endsWith(suffix)) =>
              logger.debug(s"suffix, 监控目录发现新数据文件, ${name.toString}, ${System.currentTimeMillis()}")
            case ENTRY_DELETE =>
            case ENTRY_MODIFY =>
            case _ =>
          }

        }
        // 为监控下一个通知做准备
        key.reset()
      }
    }
    case Terminated(putActor) => logger.error(s"putActor 出现异常")
  }

  /**
   * java FileWatcher 实现目录监控
   */
  def cast[T: Manifest](event: WatchEvent[T]): WatchEvent[T] = {
    event.asInstanceOf[WatchEvent[T]]
  }


  /**
   * 计算分区格式
   * @param partitions
   * @param format
   * @return
   */
  def partitionData(partitions: String, format: String = "yyyyMM/dd"): String = {

    partitions match {
      // 按天分区
      case "24" => DateUtils.systemDate("yyyyMM/dd")
      // 按小时分区
      case "60" => ""
      case _ => ""
    }

  }

}

object WatcherServer {


 // val logger: Logger = LoggerFactory.getLogger(WatcherServer.getClass)

  def main(args: Array[String]): Unit = {


    // 从配置文件中读取数据
    val m = new Metadata(FromFilePath("/opt/test.properties"))

    val watcher_path = m.prop.getProperty("watcher_path")
    val watcher_files = m.prop.getProperty("watcher_files")
    val hdfs_path = m.prop.getProperty("hdfs_path")
    //val dest_files = m.prop.getProperty("dest_files")
    val dest_files_part = m.prop.getProperty("dest_files_part", "24")
    val back_path = m.prop.getProperty("back_path", s"${m.prop.getProperty("watcher_path")}/back")
    val error_path = m.prop.getProperty("error_path", s"${m.prop.getProperty("watcher_path")}/error")
    val suffix = m.prop.getProperty("suffix", ".csv")
    val check_suffix = m.prop.getProperty("check_suffix", ".ok")

    val watcher_files_array = watcher_files.split(",", -1)
    val dest_files_part_array = dest_files_part.split(",", -1)

    // 初始化akka actor
    val as = ActorSystem("FileWatcher")

    // 判断是否设置了多目录多分区
    (watcher_files_array.length, dest_files_part_array.length) match {
      // 自动读取监控目录下的文件夹, 并且所有目录一个分区
      case (0, 1) =>

      // 配置需要读取的文件夹,  并且所有目录一个分区
      case (x, 1) if (x > 0) =>
        // 遍历所有的待监控文件夹
        watcher_files_array.foreach { lines =>
          // 为每个文件夹创建actor
          val actor = as.actorOf(Props[WatcherServer], name = s"WatcherServer-$lines")
          // 发送监控信息
          actor ! WatcherServerBean(s"${watcher_path}/$lines", s"${hdfs_path}/$lines", dest_files_part, back_path, error_path, suffix, check_suffix)
        }
    }

  }
}
