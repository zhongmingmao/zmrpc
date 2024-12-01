package ai.zhongmingmao.zmrpc.core.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcRequest {
  String service;
  String method;
  Object[] args;
}
