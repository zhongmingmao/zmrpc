package ai.zhongmingmao.zmrpc.core.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcContext {
  List<Filter> filters;
  Router router;
  LoadBalancer loadBalancer;
}
