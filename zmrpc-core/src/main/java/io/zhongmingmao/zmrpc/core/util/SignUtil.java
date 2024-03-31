package io.zhongmingmao.zmrpc.core.util;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SignUtil {

  public static String buildRequestSign(final RpcRequest request) {
    if (Objects.isNull(request)) {
      return "";
    }

    StringBuilder sign = new StringBuilder();

    sign.append(request.getService());
    sign.append('#');
    sign.append(request.getMethod());
    sign.append('(');
    buildRequestArgsSign(request.getArgs()).ifPresent(sign::append);
    sign.append(')');

    return sign.toString();
  }

  private static Optional<String> buildRequestArgsSign(final RpcRequestArg[] args) {
    return Optional.ofNullable(args)
        .map(Arrays::stream)
        .map(stream -> stream.map(RpcRequestArg::getType).collect(Collectors.joining(",")));
  }
}
