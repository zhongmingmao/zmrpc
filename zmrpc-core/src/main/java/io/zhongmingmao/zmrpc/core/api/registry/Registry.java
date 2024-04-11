package io.zhongmingmao.zmrpc.core.api.registry;

import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedListener;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Service;

import java.util.List;

public interface Registry {

  void start();

  void stop();

  void register(final Instance instance);

  void unregister(final Instance instance);

  List<String> fetchInstances(final Service service);

  void subscribe(final Service service, final RegistryChangedListener listener);
}
