package ai.zhongmingmao.zmrpc.core.api;

import org.springframework.util.CollectionUtils;

import java.util.List;

public interface LoadBalancer<T> {

  T choose(List<T> providers);

  LoadBalancer DEFAULT = providers -> CollectionUtils.isEmpty(providers) ? null : providers.get(0);
}
