package me.zhongmingmao.zmrpc.core.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RpcContext {
  List<Filter> filters;
  Router router;
  LoadBalancer loadBalancer;
}
