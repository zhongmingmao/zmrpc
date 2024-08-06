package me.zhongmingmao.zmrpc.core.api;

import java.util.List;

public interface LoadBalancer<T> {
  T choose(List<T> providers);

  LoadBalancer Default = p -> p == null || p.size() == 0 ? null : p.get(0);
}
