package ai.zhongmingmao.zmrpc.core.cluster;

import ai.zhongmingmao.zmrpc.core.api.LoadBalancer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

  Random random = new Random();

  @Override
  public T choose(List<T> providers) {
    return CollectionUtils.isEmpty(providers)
        ? null
        : providers.size() == 1
            ? providers.get(0)
            : providers.get(random.nextInt(providers.size()));
  }
}
