package io.zhongmingmao.zmrpc.core.api.context;

import io.zhongmingmao.zmrpc.core.api.lb.filter.Filter;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.LoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.router.Router;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Service;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcContext {
  Service service;
  List<Instance> providers;

  Filter<Instance> filter;
  Router<Instance> router;
  LoadBalancer<Instance> loadBalancer;
}
