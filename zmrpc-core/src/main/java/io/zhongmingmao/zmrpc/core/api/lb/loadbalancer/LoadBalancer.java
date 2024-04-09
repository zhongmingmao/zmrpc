package io.zhongmingmao.zmrpc.core.api.lb.loadbalancer;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface LoadBalancer<T> {

  Optional<T> choose(final List<T> providers);
}
