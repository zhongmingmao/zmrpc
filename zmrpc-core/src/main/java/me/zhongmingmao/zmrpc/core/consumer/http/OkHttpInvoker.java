package me.zhongmingmao.zmrpc.core.consumer.http;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.api.RpcException;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import okhttp3.*;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {

  static final MediaType APPLICATION_JSON = MediaType.get("application/json");

  OkHttpClient client;

  public OkHttpInvoker(int timeout) {
    client =
        new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build();
  }

  @Override
  public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
    // 序列化请求
    String reqJson = JSON.toJSONString(rpcRequest);

    Request request =
        new Request.Builder().url(url).post(RequestBody.create(reqJson, APPLICATION_JSON)).build();

    try {
      log.debug("===> reqJson = " + reqJson);
      String resJson = client.newCall(request).execute().body().string();
      log.debug("===> resJson = " + resJson);
      // 反序列化响应
      RpcResponse<Object> rpcResponse = JSON.parseObject(resJson, RpcResponse.class);
      return rpcResponse;
    } catch (IOException e) {
      throw new RpcException(e, RpcException.HttpException);
    }
  }
}
