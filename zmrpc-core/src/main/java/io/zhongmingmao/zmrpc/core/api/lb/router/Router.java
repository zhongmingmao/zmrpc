package io.zhongmingmao.zmrpc.core.api.lb.router;

import java.util.List;

@FunctionalInterface
public interface Router<T> {

  Router<?> DEFAULT = providers -> providers;

  List<T> route(List<T> providers);
}
