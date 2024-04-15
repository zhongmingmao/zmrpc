package io.zhongmingmao.zmrpc.core.api.error;

public interface RpcExceptions {

  static RpcException newTechErr(final String message) {
    return newErr(RpcExceptionType.TECH, message);
  }

  static RpcException newTechErr(final String message, final Throwable cause) {
    return newErr(RpcExceptionType.TECH, message, cause);
  }

  static RpcException newBizErr(final String message) {
    return newErr(RpcExceptionType.BIZ, message);
  }

  static RpcException newBizErr(final String message, final Throwable cause) {
    return newErr(RpcExceptionType.BIZ, message, cause);
  }

  static RpcException newUnknownErr(final String message) {
    return newErr(RpcExceptionType.UNKNOWN, message);
  }

  static RpcException newUnknownErr(final String message, final Throwable cause) {
    return newErr(RpcExceptionType.UNKNOWN, message, cause);
  }

  static RpcException newErr(final RpcExceptionType type, final String message) {
    return new RpcException(message, type);
  }

  static RpcException newErr(
      final RpcExceptionType type, final String message, final Throwable cause) {
    return new RpcException(message, cause, type);
  }
}
