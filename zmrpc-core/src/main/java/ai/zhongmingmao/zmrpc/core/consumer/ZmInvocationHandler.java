package ai.zhongmingmao.zmrpc.core.consumer;

import ai.zhongmingmao.zmrpc.core.api.RpcRequest;
import ai.zhongmingmao.zmrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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

    String name = method.getName();
    if ("toString".equals(name)
        || "getClass".equals(name)
        || "notify".equals(name)
        || "notifyAll".equals(name)
        || "wait".equals(name)) {
      return null;
    }

    if ("hashCode".equals(name)) {
      return 0;
    }

    if ("equals".equals(name)) {
      return false;
    }

    RpcRequest request =
        RpcRequest.builder().service(service.getCanonicalName()).method(name).args(args).build();

    RpcResponse response = post(request);
    if (response.isStatus()) {
      Object data = response.getData();
      if (data instanceof JSONObject) {
        JSONObject jsonResult = (JSONObject) response.getData();
        return jsonResult.toJavaObject(method.getReturnType());
      } else {
        return data;
      }
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
