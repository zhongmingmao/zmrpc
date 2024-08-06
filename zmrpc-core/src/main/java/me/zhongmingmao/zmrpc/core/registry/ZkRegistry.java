package me.zhongmingmao.zmrpc.core.registry;

import me.zhongmingmao.zmrpc.core.api.RegistryCenter;

import java.util.List;

public class ZkRegistry implements RegistryCenter {
  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void register(String service, String instance) {}

  @Override
  public void unregister(String service, String instance) {}

  @Override
  public List<String> fetchAll(String service) {
    return null;
  }
}
