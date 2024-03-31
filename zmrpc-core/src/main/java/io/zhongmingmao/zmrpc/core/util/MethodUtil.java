package io.zhongmingmao.zmrpc.core.util;

import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

public final class MethodUtil {

  private static final Set<String> RESERVED_METHODS = Sets.newHashSet();

  static {
    for (Method method : Object.class.getMethods()) {
      if (Objects.equals(Object.class, method.getDeclaringClass())) {
        RESERVED_METHODS.add(method.getName());
      }
    }
  }

  public static boolean isReservedMethod(final String method) {
    return RESERVED_METHODS.contains(method);
  }
}
