package io.zhongmingmao.zmrpc.core.api.registry;

import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedEvent;
import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedListener;
import java.util.Collections;
import java.util.List;
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
      log.error(
          "failed to connect to zookeeper, address: %s, namespace: %s"
              .formatted(address, namespace),
          e);
    }
  }

  @Override
  public void stop() {
    try {
      client.close();
    } catch (Exception e) {
      log.error("failed to close client", e);
    }
  }

  @Override
  public void register(String service, String instance) {
    try {
      String servicePath = buildPath(service);
      if (Objects.isNull(client.checkExists().forPath(servicePath))) {
        client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
        log.debug("create persistent node, servicePath: {}", servicePath);
      }

      String instancePath = buildPath(service, instance);
      if (Objects.isNull(client.checkExists().forPath(instancePath))) {
        client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "instance".getBytes());
        log.debug("create ephemeral node, instancePath: {}", instancePath);
      }
      log.info("register successfully, service: {}, instance: {}", service, instance);
    } catch (Exception e) {
      log.error("register fail, service: %s, instance: %s".formatted(service, instance), e);
    }
  }

  @Override
  public void unregister(String service, String instance) {
    try {
      if (Objects.isNull(client.checkExists().forPath(buildPath(service)))) {
        log.warn("'{}' has not been registered yet, unable to unregister", service);
        return;
      }

      String instancePath = buildPath(service, instance);
      if (Objects.isNull(client.checkExists().forPath(instancePath))) {
        log.warn("'{}/{}' has not been registered yet, unable to unregister", service, instance);
        return;
      }

      client.delete().quietly().forPath(instancePath);
      log.info("unregister successfully, service: {}, instance: {}", service, instance);
    } catch (Exception e) {
      log.error("unregister fail, service: %s, instance: %s".formatted(service, instance), e);
    }
  }

  @Override
  public List<String> fetchInstances(String service) {
    try {
      return client.getChildren().forPath(buildPath(service));
    } catch (Exception e) {
      log.error("fetchInstances fail, service: %s".formatted(service), e);
      return Collections.emptyList();
    }
  }

  @Override
  public void subscribe(String service, RegistryChangedListener listener) {
    try {
      TreeCache cache =
          TreeCache.newBuilder(client, buildPath(service))
              .setCacheData(true)
              .setMaxDepth(1 << 1)
              .build();
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
      log.info("subscribe successfully, service: {}", service);
    } catch (Exception e) {
      log.error("subscribe fail, service: %s".formatted(service), e);
    }
  }

  private String buildPath(final String service) {
    return "/" + service;
  }

  private String buildPath(final String service, final String instance) {
    return String.join("/", buildPath(service), instance);
  }
}
