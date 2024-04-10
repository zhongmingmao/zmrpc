package io.zhongmingmao.zmrpc.core.api.registry.event;

@FunctionalInterface
public interface RegistryChangedListener {
  void handle(final RegistryChangedEvent event);
}
