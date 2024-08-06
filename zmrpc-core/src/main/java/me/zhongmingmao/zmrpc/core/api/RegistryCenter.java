package me.zhongmingmao.zmrpc.core.api;

import lombok.AllArgsConstructor;

import java.util.List;

public interface RegistryCenter {
  void start(); // for registry

  void stop(); // for registry

  void register(String service, String instance); // for provider

  void unregister(String service, String instance); // for provider

  List<String> fetchAll(String service); // for consumer

  //  void subscribe(); // for consumer

  @AllArgsConstructor
  class StaticRegistryCenter implements RegistryCenter {

    List<String> providers;

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
      return providers;
    }
  }
}
