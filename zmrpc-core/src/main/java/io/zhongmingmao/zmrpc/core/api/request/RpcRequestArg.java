package io.zhongmingmao.zmrpc.core.api.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcRequestArg {
  String type;
  Object value;
}
