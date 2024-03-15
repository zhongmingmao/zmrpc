package io.zhongmingmao.zmrpc.core.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RpcResponse {
  boolean success;
  Object data;
}
