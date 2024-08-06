package me.zhongmingmao.zmrpc.core.cluster;

import me.zhongmingmao.zmrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

  AtomicInteger index = new AtomicInteger(0);

  @Override
  public T choose(List<T> providers) {
    if (providers == null || providers.size() == 0) {
      return null;
    }

    // 按位与 - 防止溢出
    return providers.get((index.getAndIncrement() & 0x7FFFFFFF) % providers.size());
  }
}
