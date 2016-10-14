package com.lb.utils

import java.io.File

/**
 * Created by liubing on 16-10-12.
 */
object FileUtils {


  /**
   * 基于文件扩展名限制返回文件列表
   * @param dir
   * @param extensions
   * @return
   */
  def getListOfFiles(dir: File, extensions: List[String]) : List[File] ={
    dir.listFiles().filter(_.isFile).toList.filter{ file =>
      extensions.exists(file.getName.endsWith(_))
    }
  }

  /**
   * 列出目录中的所有文件
   * @param dir
   * @return
   */
  def getListOfFiles(dir: File) : List[File] ={
    dir.listFiles.filter(_.isFile).toList
  }

  /**
   * 列出目录中的所有子目录
   * @param dir
   * @return
   */
  def getListOfDirectories(dir: File) : List[File] ={
    dir.listFiles.filter(_.isDirectory).toList
  }



}
