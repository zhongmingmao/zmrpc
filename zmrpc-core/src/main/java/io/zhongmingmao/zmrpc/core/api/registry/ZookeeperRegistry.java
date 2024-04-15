package io.zhongmingmao.zmrpc.core.api.registry;

import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.api.error.RpcExceptions;
import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedEvent;
import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedListener;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Service;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.core.env.Environment;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZookeeperRegistry implements Registry {

  private static final String PROPERTY_ZOOKEEPER_ADDRESS = "zmrpc.registry.zookeeper.address";
  private static final String PROPERTY_ZOOKEEPER_NAMESPACE = "zmrpc.registry.zookeeper.namespace";

  final Environment environment;
  CuratorFramework client;

  Map<String, TreeCache> caches = Maps.newConcurrentMap();

  @Override
  public void start() {
    String address = environment.getProperty(PROPERTY_ZOOKEEPER_ADDRESS, "localhost:2181");
    String namespace = environment.getProperty(PROPERTY_ZOOKEEPER_NAMESPACE, "zmrpc");
    log.info("try to connect to zookeeper, address: {}, namespace: {}", address, namespace);
    try {
      client =
          CuratorFrameworkFactory.builder()
              .connectString(address)
              .namespace(namespace)
              .retryPolicy(new ExponentialBackoffRetry(1 << 10, 1 << 2))
              .build();
      client.start();
      log.info(
          "successfully connected to zookeeper, address: {}, namespace: {}", address, namespace);
    } catch (Exception e) {
      String message =
          "failed to connect to zookeeper, address: %s, namespace: %s"
              .formatted(address, namespace);
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }

  @Override
  public void stop() {
    try {
      caches.forEach(
          (service, cache) -> {
            cache.close();
            log.info("successfully close tree cache, service: {}", service);
          });

      client.close();
      log.info("successfully close client");
    } catch (Exception e) {
      String message = "failed to close client";
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }

  @Override
  public void register(Instance instance) {
    try {
      String servicePath = instance.getService().buildRegistryPath();
      if (Objects.isNull(client.checkExists().forPath(servicePath))) {
        client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
        log.debug("create persistent node, servicePath: {}", servicePath);
      }

      String instancePath = instance.buildRegistryPath();
      if (Objects.isNull(client.checkExists().forPath(instancePath))) {
        client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "instance".getBytes());
        log.debug("create ephemeral node, instancePath: {}", instancePath);
      }
      log.info("register successfully, service: {}, instance: {}", instance.getService(), instance);
    } catch (Exception e) {
      String message =
          "register fail, service: %s, instance: %s".formatted(instance.getService(), instance);
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }

  @Override
  public void unregister(Instance instance) {
    try {
      if (Objects.isNull(client.checkExists().forPath(instance.getService().buildRegistryPath()))) {
        log.warn("'{}' has not been registered yet, unable to unregister", instance.getService());
        return;
      }

      String instancePath = instance.buildRegistryPath();
      if (Objects.isNull(client.checkExists().forPath(instancePath))) {
        log.warn(
            "'{}/{}' has not been registered yet, unable to unregister",
            instance.getService(),
            instance);
        return;
      }

      client.delete().quietly().forPath(instancePath);
      log.info(
          "unregister successfully, service: {}, instance: {}", instance.getService(), instance);
    } catch (Exception e) {
      String message =
          "unregister fail, service: %s, instance: %s".formatted(instance.getService(), instance);
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }

  @Override
  public List<String> fetchInstances(Service service) {
    try {
      return client.getChildren().forPath(service.buildRegistryPath());
    } catch (Exception e) {
      String message = "fetchInstances fail, service: %s".formatted(service);
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }

  @Override
  public void subscribe(Service service, RegistryChangedListener listener) {
    try {
      String registryPath = service.buildRegistryPath();
      TreeCache cache =
          TreeCache.newBuilder(client, registryPath).setCacheData(true).setMaxDepth(1 << 1).build();
      cache
          .getListenable()
          .addListener(
              (curator, event) -> {
                log.info("receive an event, service: {}, event: {}", service, event);
                listener.handle(
                    RegistryChangedEvent.builder()
                        .service(service)
                        .instances(fetchInstances(service))
                        .build());
              });
      cache.start();
      caches.putIfAbsent(registryPath, cache);
      log.info("subscribe successfully, service: {}", service);
    } catch (Exception e) {
      String message = "subscribe fail, service: %s".formatted(service);
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }
}
