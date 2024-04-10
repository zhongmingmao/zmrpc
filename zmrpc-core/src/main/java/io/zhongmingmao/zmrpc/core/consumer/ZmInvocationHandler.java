package io.zhongmingmao.zmrpc.core.consumer;

import static io.zhongmingmao.zmrpc.core.consumer.ConsumerUtil.buildRequestArgs;
import static io.zhongmingmao.zmrpc.core.util.HttpUtil.JSON;
import static io.zhongmingmao.zmrpc.core.util.JsonUtil.*;

import com.google.common.base.Defaults;
import io.zhongmingmao.zmrpc.core.api.context.RpcContext;
import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedEvent;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.util.GenericUtil;
import io.zhongmingmao.zmrpc.core.util.HttpUtil;
import io.zhongmingmao.zmrpc.core.util.MethodUtil;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
@Data
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZmInvocationHandler implements InvocationHandler {

  OkHttpClient client = HttpUtil.buildClient();

  RpcContext<String> context;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    Class<?> returnType = method.getReturnType();
    if (MethodUtil.isReservedMethod(methodName)) {
      log.error(methodName + " is reserved");
      return Defaults.defaultValue(returnType);
    }

    RpcRequest request =
        RpcRequest.builder()
            .service(context.getService())
            .method(methodName)
            .args(buildRequestArgs(method.getParameterTypes(), args))
            .build();
    Optional<RpcResponse> response =
        HttpUtil.execute(client, buildHttpRequest(request), RpcResponse.class);

    if (response.isPresent() && response.get().isSuccess()) {
      return GenericUtil.buildValue(
          returnType, method.getGenericReturnType(), response.get().getData());
    }

    return Defaults.defaultValue(returnType);
  }

  public void refreshContext(final RegistryChangedEvent event) {
    if (!Objects.equals(event.getService(), context.getService())) {
      log.warn(
          "refreshContext fail, service mismatch, event-service: {}, context-service: {}",
          event.getService(),
          context.getService());
      return;
    }
    context.setProviders(
        event.getInstances().stream()
            .map(ConsumerUtil::buildProvider)
            .collect(Collectors.toList()));
  }

  private Request buildHttpRequest(final RpcRequest request) {
    return new Request.Builder()
        .url(chooseProvider().orElseThrow(() -> new RuntimeException("find no providers")))
        .post(RequestBody.create(toJsonOrEmpty(request), JSON))
        .build();
  }

  private Optional<String> chooseProvider() {
    return context.getLoadBalancer().choose(context.getRouter().route(context.getProviders()));
  }
}
