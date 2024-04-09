package io.zhongmingmao.zmrpc.core.api.lb.loadbalancer;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

  Random random = new Random();

  @Override
  public Optional<T> choose(List<T> providers) {
    return CollectionUtils.isEmpty(providers)
        ? Optional.empty()
        : Optional.ofNullable(providers.get(random.nextInt(providers.size())));
  }
}
