package me.zhongmingmao.zmrpc.core.api;

import lombok.AllArgsConstructor;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.provider.ServiceMeta;
import me.zhongmingmao.zmrpc.core.registry.ChangeListener;

import java.util.List;

public interface RegistryCenter {
  void start(); // for registry

  void stop(); // for registry

  void register(ServiceMeta service, InstanceMeta instance); // for provider

  void unregister(ServiceMeta service, InstanceMeta instance); // for provider

  List<InstanceMeta> fetchAll(ServiceMeta service); // for consumer

  // ChangeListener - 事件监听器，将变化向外传递
  void subscribe(ServiceMeta service, ChangeListener listener); // for consumer

  @AllArgsConstructor
  class StaticRegistryCenter implements RegistryCenter {

    List<InstanceMeta> providers;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {}

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {}

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
      return providers;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangeListener listener) {}
  }
}
