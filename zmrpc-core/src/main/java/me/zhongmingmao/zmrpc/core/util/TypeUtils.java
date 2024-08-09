package me.zhongmingmao.zmrpc.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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

  @Nullable
  public static Object castMethodResult(Method method, Object data) {
    // 需进行数据类型转换
    Class<?> returnType = method.getReturnType();

    if (data instanceof JSONObject jsonResult) {
      if (Map.class.isAssignableFrom(returnType)) { // 返回值为 Map 类型
        Map<Object, Object> resultMap = new HashMap<>();
        Type genericReturnType = method.getGenericReturnType();
        // genericReturnType ==> java.util.Map<java.lang.String,
        // me.zhongmingmao.zmrpc.demo.api.User>
        System.out.println("genericReturnType ==> " + genericReturnType);
        if (genericReturnType instanceof ParameterizedType parameterizedType) {
          Type[] actualTypes = parameterizedType.getActualTypeArguments();
          Class<?> keyType = (Class<?>) actualTypes[0];
          Class<?> valueType = (Class<?>) actualTypes[1];
          // class java.lang.String
          System.out.println("keyType ==> " + keyType);
          // class me.zhongmingmao.zmrpc.demo.api.User
          System.out.println("valueType ==> " + valueType);
          jsonResult.forEach(
              (k, v) -> {
                Object key = TypeUtils.cast(k, keyType);
                Object value = TypeUtils.cast(v, valueType);
                resultMap.put(key, value);
              });
        }
        return resultMap;
      }

      // 反序列化后为一个 JSONObject，并非预期的类型，需进行类型转换
      return jsonResult.toJavaObject(returnType);
    } else if (data instanceof JSONArray jsonArray) {
      Object[] array = jsonArray.toArray();

      if (returnType.isArray()) { // 返回值为数组类型
        Class<?> componentType = returnType.getComponentType(); // 数组元素的类型
        System.out.println("componentType => " + componentType.getCanonicalName());
        Object resultArray = Array.newInstance(componentType, array.length); // 创建预期类型的数组
        for (int i = 0; i < array.length; i++) {
          Object component = array[i];
          if (!componentType.isPrimitive() && !componentType.getPackageName().startsWith("java")) {
            component = TypeUtils.cast(component, componentType);
          }
          Array.set(resultArray, i, component); // 通过反射为数组元素赋值
        }
        return resultArray;
      } else if (List.class.isAssignableFrom(returnType)) { // 返回值为 List 类型
        List<Object> resultList = new ArrayList<>(array.length);
        Type genericReturnType = method.getGenericReturnType(); // 获取泛型类型
        // java.util.List<me.zhongmingmao.zmrpc.demo.api.User>
        System.out.println("genericReturnType ==> " + genericReturnType);
        if (genericReturnType instanceof ParameterizedType parameterizedType) { // 参数化类型
          Type actualType = parameterizedType.getActualTypeArguments()[0];
          // class me.zhongmingmao.zmrpc.demo.api.User
          System.out.println("actualType ==> " + actualType);
          for (Object o : array) {
            resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
          }
        } else {
          resultList.add(Arrays.asList(array));
        }
        return resultList;
      } else {
        return null;
      }
    } else {
      // fast-json 没有将 Response 反序列化为 JSONObject，适用于原生类型，String 等
      return TypeUtils.cast(data, method.getReturnType());
    }
  }
}
