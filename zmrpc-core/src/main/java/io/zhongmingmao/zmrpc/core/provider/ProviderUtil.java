package io.zhongmingmao.zmrpc.core.provider;

import static io.zhongmingmao.zmrpc.core.util.JsonUtil.*;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;
import io.zhongmingmao.zmrpc.core.util.GenericUtil;
import io.zhongmingmao.zmrpc.core.util.ReflectUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public final class ProviderUtil {

  private static final Class<?>[] EMPTY_TYPES = new Class<?>[0];
  private static final Object[] EMPTY_ARGS = new Object[0];

  public static Class<?>[] buildRequestArgTypes(final RpcRequestArg[] args) {
    if (Objects.isNull(args)) {
      return EMPTY_TYPES;
    }
    return Arrays.stream(args)
        .map(arg -> ReflectUtil.findClass(arg.getType()))
        .toArray(Class<?>[]::new);
  }

  /**
   * {@link Method#getParameterTypes()} ()} <br>
   * {@link Method#getGenericParameterTypes()}
   */
  public static Object[] buildRequestArgValues(
      final Class<?>[] parameterTypes,
      final Type[] genericParameterTypes,
      final RpcRequestArg[] args) {
    if (Objects.isNull(args)) {
      return EMPTY_ARGS;
    }

    Object[] values = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      values[i] =
          GenericUtil.buildValue(parameterTypes[i], genericParameterTypes[i], args[i].getValue());
    }
    return values;
  }
}
