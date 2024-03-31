package io.zhongmingmao.zmrpc.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.zhongmingmao.zmrpc.core.util.JsonUtil.*;
import static io.zhongmingmao.zmrpc.core.util.JsonUtil.toJsonOrEmpty;

public final class GenericUtil {

  public static Object buildValue(final Class<?> type, final Type genericType, final Object value) {
    Object v = value;

    if (Objects.nonNull(v)) {
      if (genericType instanceof ParameterizedType) {
        // List
        if (List.class.isAssignableFrom(type)) {
          Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
          if (elementType instanceof Class<?>) {
            v = fromJsonListOrNull(toJsonOrEmpty(v), (Class<?>) elementType);
          }
        }

        // Set
        if (Set.class.isAssignableFrom(type)) {
          Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
          if (elementType instanceof Class<?>) {
            v = fromJsonSetOrNull(toJsonOrEmpty(v), (Class<?>) elementType);
          }
        }

        // Map
        if (Map.class.isAssignableFrom(type)) {
          Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
          Type keyType = actualTypes[0];
          Type valueType = actualTypes[1];
          if (keyType instanceof Class<?> && valueType instanceof Class<?>) {
            v = fromJsonMapOrNull(toJsonOrEmpty(v), (Class) keyType, (Class) valueType);
          }
        }
      } else {
        v = fromJsonOrNull(toJsonOrEmpty(v), type);
      }
    }

    return v;
  }
}
