package me.zhongmingmao.zmrpc.core.api;

import lombok.AllArgsConstructor;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.registry.ChangeListener;

import java.util.List;

public interface RegistryCenter {
  void start(); // for registry

  void stop(); // for registry

  void register(String service, InstanceMeta instance); // for provider

  void unregister(String service, InstanceMeta instance); // for provider

  List<InstanceMeta> fetchAll(String service); // for consumer

  // ChangeListener - 事件监听器，将变化向外传递
  void subscribe(String service, ChangeListener listener); // for consumer

  @AllArgsConstructor
  class StaticRegistryCenter implements RegistryCenter {

    List<InstanceMeta> providers;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void register(String service, InstanceMeta instance) {}

    @Override
    public void unregister(String service, InstanceMeta instance) {}

    @Override
    public List<InstanceMeta> fetchAll(String service) {
      return providers;
    }

    @Override
    public void subscribe(String service, ChangeListener listener) {}
  }
}
