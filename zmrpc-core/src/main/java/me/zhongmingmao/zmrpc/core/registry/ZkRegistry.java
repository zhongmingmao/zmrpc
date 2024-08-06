package me.zhongmingmao.zmrpc.core.registry;

import java.util.List;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class ZkRegistry implements RegistryCenter {

  private CuratorFramework client = null;

  @Override
  public void start() {
    System.out.println("zk client start");
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    client =
        CuratorFrameworkFactory.builder()
            .connectString("arch:2181")
            .namespace("zmrpc")
            .retryPolicy(retryPolicy)
            .build();
    client.start();
  }

  @Override
  public void stop() {
    System.out.println("zk client stop");
    client.close();
  }

  @Override
  public void register(String service, String instance) {
    String servicePath = "/" + service;

    try {
      // service 注册为持久化节点
      if (client.checkExists().forPath(servicePath) == null) {
        client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
      }

      // instance 注册为临时节点 - 不断变化
      String instancePath = servicePath + "/" + instance;
      if (client.checkExists().forPath(instancePath) == null) {
        System.out.println("===> register to zk, instancePath: " + instancePath);
        client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void unregister(String service, String instance) {
    String servicePath = "/" + service;

    try {
      if (client.checkExists().forPath(servicePath) == null) {
        // service 不存在，直接返回
        return;
      }

      String instancePath = servicePath + "/" + instance;
      if (client.checkExists().forPath(instancePath) == null) {
        // instance 不存在，直接返回
        return;
      }

      System.out.println("===> unregister from zk, instancePath: " + instancePath);
      client.delete().quietly().forPath(instancePath); // 删除 instance
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> fetchAll(String service) {
    return null;
  }
}
