package io.zhongmingmao.zmrpc.core.api.registry;

import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedListener;

import java.util.List;

public interface Registry {

  void start();

  void stop();

  void register(final String service, final String instance);

  void unregister(final String service, final String instance);

  List<String> fetchInstances(final String service);

  void subscribe(final String service, final RegistryChangedListener listener);
}
