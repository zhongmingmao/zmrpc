package io.zhongmingmao.zmrpc.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.annotatation.ZmConsumer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Consumer:<br>
 * {@link java.lang.reflect.Method#getParameterTypes()}<br>
 * {@link Class#getCanonicalName()}<br>
 * <br>
 * Provider:<br>
 * {@link java.lang.Class#getMethod(String, Class[])}
 */
@Slf4j
public final class ReflectUtil {

  private static final Map<String, Class<?>> TYPE_CACHE = Maps.newConcurrentMap();

  static {
    TYPE_CACHE.put("boolean", boolean.class);
    TYPE_CACHE.put("char", char.class);
    TYPE_CACHE.put("byte", byte.class);
    TYPE_CACHE.put("short", short.class);
    TYPE_CACHE.put("int", int.class);
    TYPE_CACHE.put("long", long.class);
    TYPE_CACHE.put("float", float.class);
    TYPE_CACHE.put("double", double.class);

    TYPE_CACHE.put("boolean[]", boolean[].class);
    TYPE_CACHE.put("char[]", char[].class);
    TYPE_CACHE.put("byte[]", byte[].class);
    TYPE_CACHE.put("short[]", short[].class);
    TYPE_CACHE.put("int[]", int[].class);
    TYPE_CACHE.put("long[]", long[].class);
    TYPE_CACHE.put("float[]", float[].class);
    TYPE_CACHE.put("double[]", double[].class);
  }

  public static List<Field> findConsumerFields(Class<?> klass) {
    // klass maybe proxied
    List<Field> fields = Lists.newArrayList();
    while (Objects.nonNull(klass)) {
      List<Field> consumerFields =
          Arrays.stream(klass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(ZmConsumer.class))
              .toList();
      fields.addAll(consumerFields);
      klass = klass.getSuperclass();
    }
    return fields;
  }

  public static Class<?> findClass(final String type) {
    TYPE_CACHE.computeIfAbsent(
        type,
        t -> {
          try {
            return Class.forName(buildJvmClassName(t));
          } catch (Exception e) {
            log.error("findClass fail, type: " + t, e);
            return null;
          }
        });
    return TYPE_CACHE.get(type);
  }

  /**
   * @param type java.lang.String[]
   * @return [Ljava.lang.String;
   */
  private static String buildJvmClassName(final String type) {
    return type.endsWith("[]") ? "[L%s;".formatted(type.substring(0, type.length() - 2)) : type;
  }
}
