package ai.zhongmingmao.zmrpc.core.registry;

import ai.zhongmingmao.zmrpc.core.api.RegistryCenter;

import java.util.List;

public class ZkRegistryCenter implements RegistryCenter {
  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void register(String service, String instance) {}

  @Override
  public void unregister(String service, String instance) {}

  @Override
  public List<String> findAll(String service) {
    return List.of();
  }
}
