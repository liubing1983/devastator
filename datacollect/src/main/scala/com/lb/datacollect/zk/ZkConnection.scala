package com.lb.datacollect.zk

import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import org.apache.curator.retry.ExponentialBackoffRetry

/**
 * Created by liubing on 16-10-10.
 */
class ZkConnection(val namespace: String = "none", val ip: String = "127.0.0.1", val port: Int = 2181) {

  def getZKConnection: CuratorFramework = {
    namespace match {
      case "none" => CuratorFrameworkFactory.builder().connectString(ip).sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(100, 3)).build()
      case _ => CuratorFrameworkFactory.builder().connectString(ip).sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(100, 3)).namespace(namespace).build()
    }
  }
}
