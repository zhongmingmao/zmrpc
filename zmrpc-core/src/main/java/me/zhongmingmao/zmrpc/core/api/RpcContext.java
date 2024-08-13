package me.zhongmingmao.zmrpc.core.api;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;

@Data
@Builder
public class RpcContext {
  List<Filter> filters;
  Router<InstanceMeta> router;
  LoadBalancer<InstanceMeta> loadBalancer;
  Map<String, String> parameters;
}
