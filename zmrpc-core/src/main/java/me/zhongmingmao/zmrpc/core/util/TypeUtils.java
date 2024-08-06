package me.zhongmingmao.zmrpc.core.util;

import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;

public class TypeUtils {

  /**
   * @param origin 被 fast json 反序列化后的对象
   * @param type method 预期的类型
   * @return
   */
  public static Object cast(Object origin, Class<?> type) {
    if (origin == null) {
      return null;
    }

    Class<?> klass = origin.getClass();
    if (type.isAssignableFrom(klass)) { // 类型是兼容的，type 是 origin 的父类
      return origin;
    }

    // JSONObject
    if (origin instanceof JSONObject jsonObject) {
      return jsonObject.toJavaObject(type);
    }

    // LinkedHashMap
    if (origin instanceof HashMap map) {
      JSONObject jsonObject = new JSONObject(map);
      return jsonObject.toJavaObject(type);
    }

    // Array
    if (type.isArray()) {
      // 会 fast-json 被反序列化为 ArrayList
      if (origin instanceof List list) {
        int length = list.size();
        Class<?> componentType = type.getComponentType();
        System.out.println("componentType => " + componentType.getCanonicalName());
        Object resultArray = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
          Object component = list.get(i);
          if (!componentType.isPrimitive() && !componentType.getPackageName().startsWith("java")) {
            component = cast(component, componentType);
          }
          Array.set(resultArray, i, component);
        }
        return resultArray;
      }
    }

    // Boolean / boolean
    if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
      return Boolean.valueOf(origin.toString());
    }

    // Byte / byte
    if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
      return Byte.valueOf(origin.toString());
    }

    // Character / char
    if (type.equals(Character.class) || type.equals(Character.TYPE)) {
      return origin.toString().charAt(0);
    }

    // Short / short
    if (type.equals(Short.class) || type.equals(Short.TYPE)) {
      return Short.valueOf(origin.toString());
    }

    // Integer / int
    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
      return Integer.valueOf(origin.toString());
    }

    // Long / long
    if (type.equals(Long.class) || type.equals(Long.TYPE)) {
      return Long.valueOf(origin.toString());
    }

    // Float / float
    if (type.equals(Float.class) || type.equals(Float.TYPE)) {
      return Float.valueOf(origin.toString());
    }

    // Double / double
    if (type.equals(Double.class) || type.equals(Double.TYPE)) {
      return Double.valueOf(origin.toString());
    }

    return null;
  }
}
