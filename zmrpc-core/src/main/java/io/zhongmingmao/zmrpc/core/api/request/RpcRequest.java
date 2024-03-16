package io.zhongmingmao.zmrpc.core.api.request;

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
  RpcRequestArg[] args;
}
