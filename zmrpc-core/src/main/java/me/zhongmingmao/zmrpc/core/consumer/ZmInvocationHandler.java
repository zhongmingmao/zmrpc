package me.zhongmingmao.zmrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import okhttp3.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;

  public ZmInvocationHandler(Class<?> service) {
    this.service = service;
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

    RpcResponse rpcResponse = post(request);

    // 需进行数据类型转换
    if (rpcResponse.isStatus()) {
      // 成功
      Object data = rpcResponse.getData();
      if (!(data
          instanceof
          JSONObject)) { // 如果 fast-json 没有将 Response 反序列化为 JSONObject，直接返回，使用于原生类型，String 等
        return data;
      }
      JSONObject jsonResult = (JSONObject) data; // 反序列化后为一个 JSONObject，并非预期的类型
      return jsonResult.toJavaObject(method.getReturnType()); // 数据类型转换
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

  private RpcResponse post(RpcRequest rpcRequest) {
    // 序列化请求
    String reqJson = JSON.toJSONString(rpcRequest);

    Request request =
        new Request.Builder()
            .url("http://127.0.0.1:8080/")
            .post(RequestBody.create(reqJson, APPLICATION_JSON))
            .build();

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
