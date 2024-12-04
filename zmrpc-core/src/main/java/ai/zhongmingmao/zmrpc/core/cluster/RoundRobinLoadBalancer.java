package ai.zhongmingmao.zmrpc.core.cluster;

import ai.zhongmingmao.zmrpc.core.api.LoadBalancer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

  AtomicInteger index = new AtomicInteger(0);

  @Override
  public T choose(List<T> providers) {
    return providers.get((index.incrementAndGet() & 0x7FFFFFFF) % providers.size());
  }
}
