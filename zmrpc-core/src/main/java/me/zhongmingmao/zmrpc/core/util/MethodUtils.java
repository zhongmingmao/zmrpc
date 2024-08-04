package me.zhongmingmao.zmrpc.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodUtils {

  public static boolean checkLocalMethod(final String method) {
    return "toString".equals(method)
        || "hashCode".equals(method)
        || "equals".equals(method)
        || "notify".equals(method)
        || "notifyAll".equals(method)
        || "wait".equals(method)
        || "getClass".equals(method);
  }

  public static boolean checkLocalMethod(final Method method) {
    return method.getDeclaringClass().equals(Object.class);
  }

  // 计算签名 - 方法名 + 方法参数（参数个数 + 参数类型）
  // 到 Provider 时也只需要比对预先计算好的签名即可，无需每次请求都进行反射操作
  public static String methodSign(Method method) {
    StringBuilder sb = new StringBuilder(method.getName());
    sb.append("@").append(method.getParameterCount());
    Arrays.stream(method.getParameterTypes()).forEach(c -> sb.append("_").append(c.getName()));
    return sb.toString();
  }
}
