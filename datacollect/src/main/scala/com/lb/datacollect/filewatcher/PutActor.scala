package com.lb.datacollect.filewatcher

import java.io.{File, OutputStream}
import java.net.URI

import akka.actor.Actor
import com.lb.utils.DateUtils
import org.apache.commons.io.FileUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.slf4j.{Logger, LoggerFactory}


case class FileToHdfsActorBean(val fileName: String, //待上传文件
                               val check_fileName: String,  // 校验文件
                               val hdfs_path: String,   // 上传目录
                               val back_path: String,   //备份目录
                               val error_path: String)  // 异常目录

/**
 * Created by liubing on 16-10-13.
 */
class FileToHdfsActor extends Actor {

  val logger: Logger = LoggerFactory.getLogger("FileToHdfsActor")

  def receive = {
    case FileToHdfsActorBean(fileName: String, check_fileName: String, hdfs_path: String, back_path: String, error_path: String) =>
      // hdfs提供的API
      val fs: FileSystem = null
      val out: OutputStream = null
      try {

        val f = new Path(fileName)
        // 上传文件到hdfs指定目录
        FileSystem.get(URI.create(hdfs_path), new Configuration).copyFromLocalFile(f, new Path(s"${hdfs_path}/${f.getName}"))
        logger.debug(s"文件：${fileName}, 上传到${hdfs_path} 完成")

        // 将文件剪切到备份目录
        var destFile = new File(s"${back_path}/${f.getName}")
        if(destFile.isFile){  // 判断文件在备份目录是否已经存在
          // 如果存在, 在文件名称后添加时间戳
          destFile = new File(s"${back_path}/${f.getName}_back_${DateUtils.systemDate("yyyyMMdd-hhmmss")}")
          logger.debug(s"文件：${fileName}, 在备份目录已经存在")
        }
        // 剪切文件
        FileUtils.moveFile(new File(fileName),  destFile)
        logger.debug(s"文件：${fileName}, 已剪切到备份目录")

        // 删除校验文件
        FileUtils.deleteQuietly(new File(check_fileName))
        logger.debug(s"校验文件：${check_fileName}, 已删除")
      } catch {
        case e: Exception =>
          // 将文件剪切到异常目录
          // 将校验文件剪切到异常目录
          logger.error("文件：" + fileName + ", 上传到" + hdfs_path + "失败 ！！", e)
      } finally {
        if (out != null) {
          out.close()
        }
        if (fs != null) {
          fs.close()
        }
      }
  }
}
