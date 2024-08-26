package me.zhongmingmao.zmrpc.core.registry.zk;

import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.api.RpcException;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.provider.ServiceMeta;
import me.zhongmingmao.zmrpc.core.registry.ChangeListener;
import me.zhongmingmao.zmrpc.core.registry.Event;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

  @Value("${zmrpc.zkServer}")
  String servers;

  @Value("${zmrpc.zkRoot}")
  String root;

  private CuratorFramework client = null;

  @Override
  public void start() {
    System.out.printf("zk client start, connect to zk, servers: %s, root: %s%n", servers, root);
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    client =
        CuratorFrameworkFactory.builder()
            .connectString(servers)
            .namespace(root)
            .retryPolicy(retryPolicy)
            .build();
    client.start();
  }

  @Override
  public void stop() { // Bean 销毁时执行
    log.info("zk client stop");
    client.close();
  }

  @Override
  public void register(ServiceMeta service, InstanceMeta instance) {
    String servicePath = "/" + service.toZkPath();

    try {
      // service 注册为持久化节点
      if (client.checkExists().forPath(servicePath) == null) {
        client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
      }

      // instance 注册为临时节点 - 不断变化
      String instancePath = servicePath + "/" + instance.toZkPath();
      if (client.checkExists().forPath(instancePath) == null) {
        log.info("===> register to zk, instancePath: " + instancePath);
        client
            .create()
            .withMode(CreateMode.EPHEMERAL)
            .forPath(instancePath, instance.toMetas().getBytes());
      }
    } catch (Exception e) {
      throw new RpcException(e, RpcException.ZookeeperException);
    }
  }

  @Override
  public void unregister(ServiceMeta service, InstanceMeta instance) {
    String servicePath = "/" + service.toZkPath();

    try {
      if (client.checkExists().forPath(servicePath) == null) {
        // service 不存在，直接返回
        return;
      }

      String instancePath = servicePath + "/" + instance.toZkPath();
      if (client.checkExists().forPath(instancePath) == null) {
        // instance 不存在，直接返回
        return;
      }

      log.info("===> unregister from zk, instancePath: " + instancePath);
      client.delete().quietly().forPath(instancePath); // 删除 instance
    } catch (Exception e) {
      throw new RpcException(e, RpcException.ZookeeperException);
    }
  }

  @Override
  public List<InstanceMeta> fetchAll(ServiceMeta service) {
    String servicePath = "/" + service.toZkPath();

    try {
      // 获取所有子节点
      List<String> nodes = client.getChildren().forPath(servicePath);
      System.out.printf(
          "===> fetchAll from zk, servicePath: %s, instances: %s%n", servicePath, nodes);

      return buildInstances(client, servicePath, nodes);
    } catch (Exception e) {
      throw new RpcException(e, RpcException.ZookeeperException);
    }
  }

  @NotNull
  private static List<InstanceMeta> buildInstances(
      CuratorFramework client, String servicePath, List<String> nodes) {
    return nodes.stream()
        .map(
            node -> {
              String[] array = node.split("_");
              InstanceMeta instanceMeta = InstanceMeta.http(array[0], Integer.parseInt(array[1]));

              byte[] bytes;
              try {
                String nodePath = servicePath + "/" + node;
                bytes = client.getData().forPath(nodePath);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              HashMap parameters = JSON.parseObject(new String(bytes), HashMap.class);
              log.debug("instance, url: {}, parameters: {}", instanceMeta.toUrl(), parameters);
              instanceMeta.setParameters(parameters);

              return instanceMeta;
            })
        .collect(Collectors.toList());
  }

  // 监听 ZK 节点变化，获取当前最新的 Provider 列表，包装成事件，发送出去，实际的 Consumer 再消费
  @SneakyThrows
  @Override
  public void subscribe(ServiceMeta service, ChangeListener listener) {
    String servicePath = "/" + service.toZkPath();
    final TreeCache cache = // ZK 在本地的镜像数据缓存，减少交互
        TreeCache.newBuilder(client, servicePath).setCacheData(true).setMaxDepth(2).build();
    cache
        .getListenable()
        .addListener(
            (curator, event) -> {
              // 监听节点变化
              log.info("zk subscribe event: " + event);
              List<InstanceMeta> nodes = fetchAll(service); // 将最新 Provider 列表通过事件的形式传递出去
              listener.fire(new Event(nodes));
            });
    cache.start();
  }
}
