package me.zhongmingmao.zmrpc.core.consumer.http;

import com.alibaba.fastjson.JSON;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpInvoker implements HttpInvoker {

  static final MediaType APPLICATION_JSON = MediaType.get("application/json");

  OkHttpClient client;

  public OkHttpInvoker() {
    client =
        new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build();
  }

  @Override
  public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
    // 序列化请求
    String reqJson = JSON.toJSONString(rpcRequest);

    Request request =
        new Request.Builder().url(url).post(RequestBody.create(reqJson, APPLICATION_JSON)).build();

    try {
      System.out.println("===> reqJson = " + reqJson);
      String resJson = client.newCall(request).execute().body().string();
      System.out.println("===> resJson = " + resJson);
      // 反序列化响应
      RpcResponse<Object> rpcResponse = JSON.parseObject(resJson, RpcResponse.class);
      return rpcResponse;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
