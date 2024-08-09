package me.zhongmingmao.zmrpc.core.api;

import lombok.Builder;
import lombok.Data;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;

import java.util.List;

@Data
@Builder
public class RpcContext {
  List<Filter> filters;
  Router<InstanceMeta> router;
  LoadBalancer<InstanceMeta> loadBalancer;
}
