package me.zhongmingmao.zmrpc.core.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcException extends RuntimeException {

  String errCode;

  public RpcException() {}

  public RpcException(String message) {
    super(message);
  }

  public RpcException(String message, Throwable cause) {
    super(message, cause);
  }

  public RpcException(Throwable cause) {
    super(cause);
  }

  public RpcException(Throwable cause, String errCode) {
    super(cause);
    this.errCode = errCode;
  }

  public RpcException(String message, Throwable cause, String errCode) {
    super(message, cause);
    this.errCode = errCode;
  }

  // X => 为技术类异常，可以考虑重试
  // Y => 为业务类异常，一般不可重试，可以直接抛出
  // Z => 未知异常，后续再归类
  public static final String ReflectException = "X001" + "-" + "reflect_error";
  public static final String HttpException = "X002" + "-" + "http_error";
  public static final String ZookeeperException = "X003" + "-" + "zookeeper_error";
  public static final String UnknownException = "Z001" + "-" + "unknown_error";
}
