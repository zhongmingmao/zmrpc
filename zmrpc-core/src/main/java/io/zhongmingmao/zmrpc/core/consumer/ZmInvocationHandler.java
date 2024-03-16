package io.zhongmingmao.zmrpc.core.consumer;

import static io.zhongmingmao.zmrpc.core.util.RpcUtil.buildRpcRequestArgs;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.constant.RpcConstant;
import io.zhongmingmao.zmrpc.core.util.HttpUtil;
import io.zhongmingmao.zmrpc.core.util.JsonUtil;
import io.zhongmingmao.zmrpc.core.util.RpcUtil;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.env.Environment;

@Slf4j
@Data
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZmInvocationHandler implements InvocationHandler {

  private static final MediaType APPLICATION_JSON =
      MediaType.get("application/json; charset=utf-8");

  OkHttpClient client = HttpUtil.buildClient();

  Environment environment;
  Class<?> service;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    Class<?> returnType = method.getReturnType();
    if (RpcConstant.PROHIBITED_METHODS.contains(method.getName())) {
      log.error(methodName + " is prohibited");
      return RpcUtil.buildDefaultValue(returnType);
    }

    RpcResponse<?> response =
        execute(
            RpcRequest.builder()
                .service(service.getCanonicalName())
                .method(methodName)
                .args(buildRpcRequestArgs(args, method.getParameterTypes()))
                .build());

    if (Objects.isNull(response) || !response.isSuccess()) {
      return RpcUtil.buildDefaultValue(returnType);
    }

    return JsonUtil.fromJsonOrNull(
        JsonUtil.toJsonOrEmpty(response.getData()), method.getReturnType());
  }

  private RpcResponse<?> execute(final RpcRequest request) {
    try (Response response = client.newCall(buildHttpRequest(request)).execute()) {
      String body = "";
      if (Objects.nonNull(response.body())) {
        body = response.body().string();
      }
      log.debug("=== execute success, response: {}", body);
      return JsonUtil.fromJsonOrNull(body, RpcResponse.class);
    } catch (IOException e) {
      log.error("execute fail, request: " + request, e);
      return null;
    }
  }

  private Request buildHttpRequest(final RpcRequest request) {
    return new Request.Builder()
        .url(Objects.requireNonNull(environment.getProperty("provider.address")))
        .post(RequestBody.create(JsonUtil.toJsonOrEmpty(request), APPLICATION_JSON))
        .build();
  }
}
