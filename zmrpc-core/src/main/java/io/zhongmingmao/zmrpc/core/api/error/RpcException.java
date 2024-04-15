package io.zhongmingmao.zmrpc.core.api.error;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcException extends RuntimeException {

  RpcExceptionType type;

  public RpcException(RpcExceptionType type) {
    this.type = type;
  }

  public RpcException(String message, RpcExceptionType type) {
    super(message);
    this.type = type;
  }

  public RpcException(String message, Throwable cause, RpcExceptionType type) {
    super(message, cause);
    this.type = type;
  }

  public RpcException(Throwable cause, RpcExceptionType type) {
    super(cause);
    this.type = type;
  }
}
