package com.lb.datacollect.filewatcher

import java.io.File

import akka.actor.Actor
import com.lb.utils.{DateUtils, FileUtils}
import org.slf4j.{Logger, LoggerFactory}

case class FileLifeCycleBean(path: String, diff: Int)

/**
 * 采集数据声明周期处理
 * 根据配置的保存周期判断back和error目录下的数据是否已经超出存储期限
 * 判断back和error目录下是否存在异常目录, 如果存在直接删除
 * Created by liubing on 16-10-18.
 */
class FileLifeCycle extends Actor {

  val logger: Logger = LoggerFactory.getLogger("FileLifeCycle")

  def receive = {
    case FileLifeCycleBean(path, diff: Int) =>

      logger.info(path)
      // 读取目录下的所有子目录
      FileUtils.getListOfDirectories(new File(path)).foreach { dirname =>
        DateUtils.fun()(dirname.getName) match {
          //　匹配目录是否大于保存周期
          case x: Int if (x > diff) =>
            org.apache.commons.io.FileUtils.deleteQuietly(dirname)
            logger.info(s"目录: ${dirname}  已删除")

          // 目录异常, 无法格式化
          case -1 =>
            org.apache.commons.io.FileUtils.deleteQuietly(dirname)
            logger.error(s"发现异常目录: ${dirname}, 已直接删除")
        }

      }
  }

}

object Test extends App {

  val logger: Logger = LoggerFactory.getLogger("Test")

  FileUtils.getListOfDirectories(new File("/opt/lb/error/a")).foreach { dirname =>
    DateUtils.fun()(dirname.getName) match {
      case x: Int if (x > 1) =>
        org.apache.commons.io.FileUtils.deleteQuietly(dirname)
        logger.info(s"目录: ${dirname}  已删除")

      case -1 =>
        org.apache.commons.io.FileUtils.deleteQuietly(dirname)
        logger.error(s"发现异常目录: ${dirname}, 已直接删除")
    }

  }
}
