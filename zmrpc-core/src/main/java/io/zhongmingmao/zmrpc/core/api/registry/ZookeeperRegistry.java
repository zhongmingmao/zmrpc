package io.zhongmingmao.zmrpc.core.api.registry;

import java.util.List;

public class ZookeeperRegistry implements Registry {

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void register(String service, String instance) {}

  @Override
  public void unregister(String service, String instance) {}

  @Override
  public List<String> fetchInstances(String service) {
    return null;
  }

  @Override
  public void subscribe() {}
}
