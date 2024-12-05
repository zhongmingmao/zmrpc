package ai.zhongmingmao.zmrpc.core.registry;

import ai.zhongmingmao.zmrpc.core.api.RegistryCenter;
import ai.zhongmingmao.zmrpc.core.utils.InstanceUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZkRegistryCenter implements RegistryCenter {

  CuratorFramework client;

  @Override
  public void start() {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    client =
        CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .namespace("zmrpc")
            .retryPolicy(retryPolicy)
            .build();
    System.out.println("zk client started");
    client.start();
  }

  @Override
  public void stop() {
    System.out.println("zk client stopped");
    client.close();
  }

  @Override
  public void register(String service, String instance) {
    String servicePath = "/%s".formatted(service);
    try {
      if (client.checkExists().forPath(servicePath) == null) {
        client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.getBytes());
      }

      String instancePath = "%s/%s".formatted(servicePath, instance);
      System.out.printf("register to zk, service: %s, instance: %s%n", service, instance);
      client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.getBytes());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void unregister(String service, String instance) {
    String servicePath = "/%s".formatted(service);
    try {
      if (client.checkExists().forPath(servicePath) == null) {
        return;
      }

      String instancePath = "%s/%s".formatted(servicePath, instance);
      System.out.printf("unregister from zk, service: %s, instance: %s%n", service, instance);
      client.delete().quietly().forPath(instancePath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> findAll(String service) {
    String servicePath = "/%s".formatted(service);
    try {
      List<String> providers =
          client.getChildren().forPath(servicePath).stream()
              .map(InstanceUtils::buildProvider)
              .collect(Collectors.toList());
      System.out.printf(
          "find all providers from zk, service: %s, providers: %s%n", service, providers);
      return providers;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  @Override
  public void subscribe(String service, ChangeListener listener) {
    TreeCache cache =
        TreeCache.newBuilder(client, "/" + service).setCacheData(true).setMaxDepth(2).build();
    cache
        .getListenable()
        .addListener(
            (curatorFramework, treeCacheEvent) -> {
              System.out.println("zk subscribe event: " + treeCacheEvent);
              List<String> providers = findAll(service);
              listener.fire(Event.builder().providers(providers).build());
            });
    cache.start();
  }
}
