package ai.zhongmingmao.zmrpc.core.consumer;

import ai.zhongmingmao.zmrpc.core.api.RpcRequest;
import ai.zhongmingmao.zmrpc.core.api.RpcResponse;
import ai.zhongmingmao.zmrpc.core.ut.TypeUtils;
import ai.zhongmingmao.zmrpc.core.utils.MethodUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import okhttp3.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;

  public ZmInvocationHandler(Class<?> service) {
    this.service = service;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (MethodUtils.checkLocalMethod(method)) {
      return null;
    }

    RpcRequest request =
        RpcRequest.builder()
            .service(service.getCanonicalName())
            .methodSign(MethodUtils.methodSign(method))
            .args(args)
            .build();

    RpcResponse response = post(request);
    if (response.isStatus()) {
      Object data = response.getData();

      if (data instanceof JSONObject jsonResult) {
        return jsonResult.toJavaObject(method.getReturnType());
      }

      if (data instanceof JSONArray jsonArray) {
        Class<?> returnType = method.getReturnType();
        if (returnType.isArray()) {
          Class<?> componentType = returnType.getComponentType();
          Object array = Array.newInstance(componentType, jsonArray.size());
          for (int i = 0; i < jsonArray.size(); i++) {
            Array.set(array, i, TypeUtils.cast(jsonArray.get(i), componentType));
          }
          return array;
        }
        if (List.class.isAssignableFrom(returnType)) {
          List list = Lists.newArrayList(jsonArray.size());
          if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            list.addAll(
                jsonArray.stream().map(o -> TypeUtils.cast(o, (Class<?>) actualType)).toList());
          } else {
            list.addAll(jsonArray);
          }
          return list;
        }
      }

      return TypeUtils.cast(data, method.getReturnType());
    }

    Exception ex = response.getEx();
    throw new RuntimeException(ex);
  }

  static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

  OkHttpClient client =
      new OkHttpClient.Builder()
          .connectionPool(new ConnectionPool(1 << 4, 60, TimeUnit.SECONDS))
          .connectTimeout(1, TimeUnit.SECONDS)
          .readTimeout(1, TimeUnit.SECONDS)
          .writeTimeout(1, TimeUnit.SECONDS)
          .build();

  private RpcResponse post(RpcRequest rpcRequest) {
    String requestJson = JSON.toJSONString(rpcRequest);
    Request request =
        new Request.Builder()
            .url("http://localhost:8080/")
            .post(RequestBody.create(requestJson.getBytes(StandardCharsets.UTF_8), JSON_TYPE))
            .build();

    try {
      System.out.println("===> requestJson: " + requestJson);
      String responseJson = client.newCall(request).execute().body().string();
      System.out.println("<=== responseJson: " + responseJson);
      return JSON.parseObject(responseJson, RpcResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
