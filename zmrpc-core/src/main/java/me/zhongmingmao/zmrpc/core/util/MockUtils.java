package me.zhongmingmao.zmrpc.core.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockUtils {

  public static Object mock(Class<?> type) {
    if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
      return (byte) 1;
    }

    if (type.equals(Short.class) || type.equals(Short.TYPE)) {
      return 1;
    }

    if (type.equals(Character.class) || type.equals(Character.TYPE)) {
      return 'A';
    }

    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
      return 1;
    }

    if (type.equals(Long.class) || type.equals(Long.TYPE)) {
      return 1024L;
    }

    if (type.equals(Float.class) || type.equals(Float.TYPE)) {
      return 0.1F;
    }

    if (type.equals(Double.class) || type.equals(Double.TYPE)) {
      return 0.1F;
    }

    if (type.equals(String.class)) {
      return "mock string";
    }

    if (List.class.isAssignableFrom(type)) {
      return new ArrayList<>();
    }

    if (Map.class.isAssignableFrom(type)) {
      return new HashMap<>();
    }

    if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      Object resultArray = Array.newInstance(componentType, 1);
      Array.set(resultArray, 0, mock(componentType));
      return resultArray;
    }

    return mockPojo(type);
  }

  @SneakyThrows
  private static Object mockPojo(Class<?> type) {
    log.debug("mockPojo, type={}", type.getCanonicalName());
    Object instance = type.getDeclaredConstructor().newInstance();
    Field[] fields = type.getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      Type genericType = field.getGenericType();
      if (genericType instanceof ParameterizedType parameterizedType) {
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        log.debug("genericType={}", Arrays.toString(actualTypeArguments));
        field.set(instance, mock(field.getType()));
      } else {
        field.set(instance, mock(field.getType()));
      }
    }
    return instance;
  }
}
