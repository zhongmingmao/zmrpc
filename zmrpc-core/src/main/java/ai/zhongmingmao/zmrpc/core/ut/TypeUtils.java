package ai.zhongmingmao.zmrpc.core.ut;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public final class TypeUtils {

  public static Object cast(Object origin, Class<?> type) {
    if (origin == null) {
      return null;
    }

    Class<?> klass = origin.getClass();
    if (type.isAssignableFrom(klass)) {
      return origin;
    }

    // java pojo
    if (origin instanceof Map map) {
      JSONObject jsonObject = new JSONObject(map);
      return jsonObject.toJavaObject(type);
    }

    // array
    if (origin instanceof List list && type.isArray()) {
      Class<?> componentType = type.getComponentType();
      Object array = Array.newInstance(componentType, list.size());
      for (int i = 0; i < list.size(); i++) {
        Array.set(array, i, cast(list.get(i), componentType));
      }
      return array;
    }

    if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
      return Byte.valueOf(origin.toString());
    }
    if (type.equals(Short.class) || type.equals(Short.TYPE)) {
      return Short.valueOf(origin.toString());
    }
    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
      return Integer.valueOf(origin.toString());
    }
    if (type.equals(Long.class) || type.equals(Long.TYPE)) {
      return Long.valueOf(origin.toString());
    }

    if (type.equals(Float.class) || type.equals(Float.TYPE)) {
      return Float.valueOf(origin.toString());
    }
    if (type.equals(Double.class) || type.equals(Double.TYPE)) {
      return Double.valueOf(origin.toString());
    }

    if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
      return Boolean.valueOf(origin.toString());
    }
    if (type.equals(Character.class) || type.equals(Character.TYPE)) {
      return origin.toString().charAt(0);
    }

    return null;
  }
}
