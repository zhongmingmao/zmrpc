package me.zhongmingmao.zmrpc.core.cluster;

import me.zhongmingmao.zmrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer<T> implements LoadBalancer<T> {

  Random random = new Random();

  @Override
  public T choose(List<T> providers) {
    if (providers == null || providers.size() == 0) {
      return null;
    }

    return providers.get(random.nextInt(providers.size()));
  }
}
