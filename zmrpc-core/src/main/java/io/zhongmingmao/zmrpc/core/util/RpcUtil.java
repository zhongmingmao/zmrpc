package io.zhongmingmao.zmrpc.core.util;

import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class RpcUtil {

  private static final Class<?>[] EMPTY_TYPES = new Class<?>[0];
  private static final Object[] EMPTY_ARGS = new Object[0];
  private static final RpcRequestArg[] EMPTY_REQUEST_ARGS = new RpcRequestArg[0];

  private static final Map<String, Class<?>> PRIMITIVE_CLASSES = Maps.newHashMap();
  private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Maps.newHashMap();

  private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Maps.newHashMap();

  static {
    PRIMITIVE_CLASSES.put("boolean", boolean.class);
    PRIMITIVE_CLASSES.put("char", char.class);
    PRIMITIVE_CLASSES.put("byte", byte.class);
    PRIMITIVE_CLASSES.put("short", short.class);
    PRIMITIVE_CLASSES.put("int", int.class);
    PRIMITIVE_CLASSES.put("long", long.class);
    PRIMITIVE_CLASSES.put("float", float.class);
    PRIMITIVE_CLASSES.put("double", double.class);
  }

  static {
    PRIMITIVE_DEFAULTS.put(boolean.class, false);
    PRIMITIVE_DEFAULTS.put(char.class, '\u0000');
    PRIMITIVE_DEFAULTS.put(byte.class, (byte) 0);
    PRIMITIVE_DEFAULTS.put(short.class, (short) 0);
    PRIMITIVE_DEFAULTS.put(int.class, 0);
    PRIMITIVE_DEFAULTS.put(long.class, 0L);
    PRIMITIVE_DEFAULTS.put(float.class, 0.0f);
    PRIMITIVE_DEFAULTS.put(double.class, 0.0d);
  }

  static {
    CONVERTERS.put(byte.class, Byte::parseByte);
    CONVERTERS.put(short.class, Short::parseShort);
    CONVERTERS.put(int.class, Integer::parseInt);
    CONVERTERS.put(long.class, Long::parseLong);
    CONVERTERS.put(float.class, Float::parseFloat);
    CONVERTERS.put(double.class, Double::parseDouble);

    CONVERTERS.put(Byte.class, Byte::parseByte);
    CONVERTERS.put(Short.class, Short::parseShort);
    CONVERTERS.put(Integer.class, Integer::parseInt);
    CONVERTERS.put(Long.class, Long::parseLong);
    CONVERTERS.put(Float.class, Float::parseFloat);
    CONVERTERS.put(Double.class, Double::parseDouble);
  }

  public static Class<?>[] buildTypes(final RpcRequestArg[] args) {
    if (Objects.isNull(args)) {
      return EMPTY_TYPES;
    }
    return Arrays.stream(args).map(arg -> findClass(arg.getType())).toArray(Class[]::new);
  }

  public static Object[] buildArgs(final RpcRequestArg... args) {
    if (Objects.isNull(args)) {
      return EMPTY_ARGS;
    }
    return Arrays.stream(args)
        .map(
            arg ->
                Optional.ofNullable(CONVERTERS.get(findClass(arg.getType())))
                    .map(converter -> converter.apply(String.valueOf(arg.getValue())))
                    .orElse(arg.getValue()))
        .toArray();
  }

  public static RpcRequestArg[] buildRpcRequestArgs(final Object[] args, final Class<?>[] types) {
    if (Objects.isNull(types) || Objects.isNull(args) || types.length != args.length) {
      return EMPTY_REQUEST_ARGS;
    }

    RpcRequestArg[] requestArgs = new RpcRequestArg[types.length];
    for (int i = 0; i < types.length; i++) {
      RpcRequestArg arg =
          RpcRequestArg.builder().type(types[i].getCanonicalName()).value(args[i]).build();
      requestArgs[i] = arg;
    }
    return requestArgs;
  }

  public static RpcRequestArg[] buildRpcRequestArgs(final Object... args) {
    if (Objects.isNull(args)) {
      return EMPTY_REQUEST_ARGS;
    }
    return Arrays.stream(args)
        .map(
            arg ->
                RpcRequestArg.builder().type(arg.getClass().getCanonicalName()).value(arg).build())
        .toArray(RpcRequestArg[]::new);
  }

  public static Object buildDefaultValue(final Class<?> type) {
    return Optional.ofNullable(PRIMITIVE_DEFAULTS.get(type))
        .orElseGet(
            () -> {
              try {
                return type.getConstructor().newInstance();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }

  private static Class<?> findClass(final String type) {
    if (PRIMITIVE_CLASSES.containsKey(type)) {
      return PRIMITIVE_CLASSES.get(type);
    }

    try {
      return Class.forName(type);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
