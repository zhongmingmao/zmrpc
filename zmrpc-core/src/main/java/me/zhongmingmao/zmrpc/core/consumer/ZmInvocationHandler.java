package me.zhongmingmao.zmrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.*;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import me.zhongmingmao.zmrpc.core.util.TypeUtils;
import okhttp3.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;
  RpcContext rpcContext;
  List<String> providers;

  public ZmInvocationHandler(Class<?> service, RpcContext rpcContext, List<String> providers) {
    this.service = service;
    this.rpcContext = rpcContext;
    this.providers = providers;
  }

  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 服务挡板
    if (MethodUtils.checkLocalMethod(method)) {
      // TBD，请求不发送到 Provider
      return null;
    }

    RpcRequest request = new RpcRequest();
    request.setService(service.getCanonicalName()); // service 为被 @ZmConsumer 修饰的字段的类型
    request.setMethodSign(MethodUtils.methodSign(method)); // 计算方法签名
    request.setArgs(args);

    List<String> urls = rpcContext.getRouter().route(providers);
    String url = (String) rpcContext.getLoadBalancer().choose(urls);
    System.out.println("select ==> " + url);
    RpcResponse rpcResponse = post(request, url);

    // 需进行数据类型转换
    Class<?> returnType = method.getReturnType();
    if (rpcResponse.isStatus()) {
      // 成功
      Object data = rpcResponse.getData();

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
            if (!componentType.isPrimitive()
                && !componentType.getPackageName().startsWith("java")) {
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
    } else {
      // 异常
      Exception ex = rpcResponse.getEx();
      // ex.printStackTrace();
      throw new RuntimeException(ex); // 直接抛出，Provider 的异常能传递到 Consumer
    }
  }

  static final MediaType APPLICATION_JSON = MediaType.get("application/json");

  OkHttpClient client =
      new OkHttpClient.Builder()
          .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
          .connectTimeout(1, TimeUnit.SECONDS)
          .readTimeout(1, TimeUnit.SECONDS)
          .writeTimeout(1, TimeUnit.SECONDS)
          .build();

  private RpcResponse post(RpcRequest rpcRequest, String url) {
    // 序列化请求
    String reqJson = JSON.toJSONString(rpcRequest);

    Request request =
        new Request.Builder().url(url).post(RequestBody.create(reqJson, APPLICATION_JSON)).build();

    try {
      System.out.println("===> reqJson = " + reqJson);
      String resJson = client.newCall(request).execute().body().string();
      System.out.println("===> resJson = " + resJson);
      // 反序列化响应
      RpcResponse rpcResponse = JSON.parseObject(resJson, RpcResponse.class);
      return rpcResponse;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
