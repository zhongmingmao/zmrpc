package me.zhongmingmao.zmrpc.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  public static List<Field> findAnnotatedField(
      Class<?> klass, Class<? extends Annotation> annotationClass) {
    List<Field> annotatedFields = new ArrayList<>();

    while (klass != null) {
      Field[] fields = klass.getDeclaredFields();
      for (Field field : fields) {
        if (field.isAnnotationPresent(annotationClass)) {
          annotatedFields.add(field);
        }
      }

      // 由于 bean.getClass() 是被 Spring 增强过的子类
      // 因此，需要不停地向上寻找 @ZmConsumer 修饰的字段
      klass = klass.getSuperclass();
    }

    return annotatedFields;
  }
}
