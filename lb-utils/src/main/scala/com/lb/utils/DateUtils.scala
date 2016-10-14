package com.lb.utils

import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Calendar, Date}

import scala.reflect.macros.ParseException

class DateUtils(val format: String = "yyyy-MM-dd")

/**
 * Created by liubing on 16-10-13.
 */
object DateUtils {

  val df1: DateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val df2: DateFormat = new SimpleDateFormat("yyyyMMdd")

  val du: DateUtils = new DateUtils

  /**
   * 获取系统时间, 并按要求格式化
   * @return
   */
  def systemDate(): String = {
    date2String(new Date(), du.format)
  }

  def systemDate(pattern: String): String = {
    date2String(new Date(), pattern)
  }

  /**
   * 按照格式化要求计算当前时间与差值的时间
   * @param pattern
   * @param diff  分钟
   * @return
   */
  def systemDateDiff(pattern: String, diff: Int): String = {
    new SimpleDateFormat(pattern).format(Calendar.getInstance().getTimeInMillis() + (diff * 60 * 1000))
  }


  /**
   * 日期按照传入格式转换成字符串
   *
   * @param date
   * @param pattern
   * @return
   */
  def date2String(date: Date, pattern: String): String = {
    new SimpleDateFormat(pattern).format(date)
  }

  /**
   * 将日期转换成String
   *
   * @param date
   * @return
   */
  def dateToyyyymmss(date: Date): String = {
    df2.format(date)
  }

  /**
   * 计算与当前天相差的天数, 并按要求格式化
   *
   * @param differnum
   * @return
   */
  def differ2day(differnum: Int, pattern: String): String = {
    var c: Calendar = Calendar.getInstance()
    c.setTime(new Date())
    c.add(Calendar.HOUR_OF_DAY, differnum * 24)
    return date2String(c.getTime(), pattern)
  }

  /**
   * String转换成date
   *
   * @param date_time
   * @return
   */
  def StringToDate(date_time: String): Date = {
    var date: Date = null
    try {
      date = df1.parse(date_time)
    } catch {
      case e: ParseException => e.printStackTrace()
    }
    date
  }

  /**
   * 获取当前时间的前一天
   * @return
   */
  def getNextDay(): Date = {
    //var date: Date = new Date()
    var calendar: Calendar = Calendar.getInstance()
    calendar.setTime(new Date)
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    calendar.getTime()
  }


}

//object DateUtils2 extends App {
//  val du: DateUtils = new DateUtils
//  println(du.systemDateDiff("yyyyMMdd HH:mm:ss", 60))
//  println(du.systemDateDiff("yyyyMMdd hh:mm:ss", 0))
//  println(du.systemDateDiff("yyyyMMdd hh:mm:ss", -60))
//  // println(du.systemDate("yyyyMMddhh"))
//  println(du.differ2day(-1, "yyyyMMdd"))
//}
