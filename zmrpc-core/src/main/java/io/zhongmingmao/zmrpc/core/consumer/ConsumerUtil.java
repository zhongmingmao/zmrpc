package io.zhongmingmao.zmrpc.core.consumer;

import com.google.common.base.Splitter;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;

import java.util.List;
import java.util.Objects;

public final class ConsumerUtil {

  private static final RpcRequestArg[] EMPTY_ARGS = new RpcRequestArg[0];

  public static RpcRequestArg[] buildRequestArgs(final Class<?>[] types, final Object[] args) {
    if (Objects.isNull(types) || Objects.isNull(args) || types.length != args.length) {
      return EMPTY_ARGS;
    }

    RpcRequestArg[] requestArgs = new RpcRequestArg[types.length];
    for (int i = 0; i < types.length; i++) {
      requestArgs[i] =
          RpcRequestArg.builder().type(types[i].getCanonicalName()).value(args[i]).build();
    }
    return requestArgs;
  }

  public static String buildProvider(final String instance) {
    List<String> items = Splitter.on("_").trimResults().omitEmptyStrings().splitToList(instance);
    return items.size() < 2
        ? null
        : "http://%s:%s/rpc/invoke".formatted(items.get(0), items.get(1));
  }
}
