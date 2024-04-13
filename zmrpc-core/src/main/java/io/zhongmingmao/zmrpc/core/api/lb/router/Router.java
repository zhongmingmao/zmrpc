package io.zhongmingmao.zmrpc.core.api.lb.router;

import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;

import java.util.List;

@FunctionalInterface
public interface Router<T> {

  Router<Instance> DEFAULT = providers -> providers;

  List<T> route(final List<T> providers);
}
