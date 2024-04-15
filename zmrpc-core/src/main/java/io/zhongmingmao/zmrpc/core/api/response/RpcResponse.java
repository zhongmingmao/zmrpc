package io.zhongmingmao.zmrpc.core.api.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcResponse<T> {
  boolean success;
  T data;
  Throwable error;
}
