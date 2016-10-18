package com.lb.utils

import java.util.Properties

import com.lb.utils.Control._
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

// 文件使用完毕后自动关闭
object Control {
  def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      if (resource != null)
        resource.close()
    }
}

abstract class FilePath
// 根据绝对路径加载配置文件
case class FromFilePath(path: String) extends FilePath
// 根据当前类的相对路径加载文件
case class ClassLoaderFilePath(path: String) extends FilePath

/**
 * Created by liubing on 16-10-10.
 */
class Metadata(path: FilePath) {

  val log: Logger = LoggerFactory.getLogger(Metadata.getClass)

  val prop: Properties = new Properties

  // println(this.getClass.getProtectionDomain().getCodeSource().getLocation().getFile())

  path match {
    case ClassLoaderFilePath(filePath) => {
      using(this.getClass.getClassLoader.getResourceAsStream(filePath)) { source =>
        prop.load(source)
      }
    }
    case FromFilePath(filePath) => {
      using(Source.fromFile(filePath).reader()) { source =>
        prop.load(source)
      }

    }
    case _ => log.error(s"获取properties方式错误, 请匹配[FromFilePath(path: String)] 或 [ClassLoaderFilePath(path: String)] !!!!!")
  }
}

object Metadata extends App {
  val m = new Metadata(FromFilePath("/opt/test.properties"))
  val log: Logger = LoggerFactory.getLogger(Metadata.getClass)
  log.debug(m.prop.getProperty("age", "123"))
  log.info(m.prop.getProperty("name", "123"))

  for(i <- 0 to 100){
    log.debug(s"$i")
  }
}
