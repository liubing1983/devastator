package com.lb.datacollect.test

import akka.actor.{Actor, ActorSystem, Props, _}
import org.slf4j.{Logger, LoggerFactory}


case class InitBean(i: Int)

class RootActor extends Actor {

  val logger: Logger = LoggerFactory.getLogger("Test")

val pror = Props(classOf[Test]).withDispatcher("my-pinned-dispatcher")
  def receive = {
    case InitBean(i) =>

      for (j <- 0 to i) {
        println(s"${Thread.currentThread().getName} =====$j")

        context.actorOf(pror) ! j
      }
  }
}

class Test extends Actor {

  val logger: Logger = LoggerFactory.getLogger("Test1")


  def receive = {
    case i: Int =>
      println(s"${Thread.currentThread().getName}--------------------------------$i")
      while (true) {
        val time = System.currentTimeMillis()
        // FileUtils.copyFile(new File("/opt/lb/b193.txt"), new File(s"/opt/lb/$i/${time}.csv"))
        // FileUtils.write(new File(s"/opt/lb/$i/${time}.ok"), "", "UTF-8", true)
        logger.info(s"$i - /opt/lb/$i/${time}.csv--${Thread.currentThread().getName}")
        Thread.sleep(3000)
      }
  }
}

/**
 * Created by liubing on 16-10-19.
 */
object TestObj extends App {

  val as = ActorSystem("test")

  val rootactor = as.actorOf(Props[RootActor].withDispatcher("my-pinned-dispatcher"), name = "rootactor")
  rootactor ! InitBean(100)

  //  val actor = as.actorOf(Props[Test], name = s"test-1")
  //  actor ! 1
  //  println(1)
  //
  //  Thread.sleep(2000)
  //  val as2 = ActorSystem("test2")
  //  val actor2 = as2.actorOf(Props[Test1], name = s"test-2")
  //  actor ! 2
  //
  //
  //  println(2)
  //  val actor3 = as.actorOf(Props[Test], name = s"test-3")
  //  actor ! 3
  //  println(3)
  //  val actor4 = as.actorOf(Props[Test1], name = s"test-4")
  //  actor ! 4
  //  println(4)
  //  val actor5 = as.actorOf(Props[Test], name = s"test-5")
  //  actor ! 5
  //  println(5)
  //  val actor6 = as.actorOf(Props[Test1], name = s"test-6")
  //  actor ! 6
  //  println(6)
  //  val actor7 = as.actorOf(Props[Test], name = s"test-7")
  //  actor ! 7
  //  println(7)
  //  val actor8 = as.actorOf(Props[Test1], name = s"test-8")
  //  actor ! 8
  //  println(8)
  //  val actor9 = as.actorOf(Props[Test], name = s"test-9")
  //  actor ! 9
  //  println(9)
  //  val actor10 = as.actorOf(Props[Test1], name = s"test-10")
  //  actor ! 10
  //  println(10)
  //  val actor11 = as.actorOf(Props[Test], name = s"test-11")
  //  actor ! 11
  //  println(11)
  //  val actor12 = as.actorOf(Props[Test1], name = s"test-12")
  //  actor ! 12
  //  println(12)
  //  val actor13 = as.actorOf(Props[Test], name = s"test-13")
  //  actor ! 13
  //  println(13)
  //  val actor14 = as.actorOf(Props[Test], name = s"test-14")
  //  actor ! 14
  //  println(14)

}
