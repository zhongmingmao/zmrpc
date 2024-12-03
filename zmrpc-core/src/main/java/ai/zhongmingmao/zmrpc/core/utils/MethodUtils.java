package ai.zhongmingmao.zmrpc.core.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodUtils {

  public static boolean checkLocalMethod(Method method) {
    return method.getDeclaringClass().equals(Object.class);
  }

  public static boolean checkLocalMethod(String methodName) {
    return "toString".equals(methodName)
        || "hashCode".equals(methodName)
        || "equals".equals(methodName)
        || "getClass".equals(methodName)
        || "notify".equals(methodName)
        || "notifyAll".equals(methodName)
        || "wait".equals(methodName);
  }

  public static String methodSign(Method method) {
    StringBuilder sb = new StringBuilder(method.getName());
    sb.append("@").append(method.getParameterCount());
    Arrays.stream(method.getParameterTypes())
        .forEach(type -> sb.append("_").append(type.getCanonicalName()));
    return sb.toString();
  }
}
