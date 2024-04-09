package io.zhongmingmao.zmrpc.core.api.lb.loadbalancer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

  AtomicInteger index = new AtomicInteger(0);

  @Override
  public Optional<T> choose(List<T> providers) {
    return CollectionUtils.isEmpty(providers)
        ? Optional.empty()
        : Optional.ofNullable(
            providers.get((index.getAndIncrement() & 0x7FFFFFFF) % providers.size()));
  }
}
