package io.zhongmingmao.zmrpc.core.api.context;

import io.zhongmingmao.zmrpc.core.api.lb.filter.Filter;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.LoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.router.Router;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcContext<T> {
  String service;
  List<T> providers;

  Filter<T> filter;
  Router<T> router;
  LoadBalancer<T> loadBalancer;
}
