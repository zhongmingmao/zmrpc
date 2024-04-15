package io.zhongmingmao.zmrpc.core.consumer;

import static io.zhongmingmao.zmrpc.core.consumer.ConsumerUtil.buildRequestArgs;
import static io.zhongmingmao.zmrpc.core.util.HttpUtil.JSON;
import static io.zhongmingmao.zmrpc.core.util.JsonUtil.*;

import com.google.common.base.Defaults;
import io.zhongmingmao.zmrpc.core.api.context.RpcContext;
import io.zhongmingmao.zmrpc.core.api.error.RpcException;
import io.zhongmingmao.zmrpc.core.api.error.RpcExceptions;
import io.zhongmingmao.zmrpc.core.api.filter.Filter;
import io.zhongmingmao.zmrpc.core.api.registry.event.RegistryChangedEvent;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.consumer.transport.http.HttpInvoker;
import io.zhongmingmao.zmrpc.core.util.GenericUtil;
import io.zhongmingmao.zmrpc.core.util.MethodUtil;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
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

  RpcContext context;
  HttpInvoker<Request> httpInvoker;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    int retries = context.getRetries();
    while (retries-- > 0) {
      try {
        log.info("retries: {}, method: {}, args: {}", retries, method, args);
        return doInvoke(proxy, method, args);
      } catch (RpcException e) {
        log.error("invoke fail, method: %s, args: %s".formatted(method, Arrays.toString(args)), e);
      }
    }
    return Defaults.defaultValue(method.getReturnType());
  }

  public Object doInvoke(Object proxy, Method method, Object[] args) throws RpcException {
    String methodName = method.getName();
    Class<?> returnType = method.getReturnType();
    if (MethodUtil.isReservedMethod(methodName)) {
      log.error(methodName + " is reserved");
      return Defaults.defaultValue(returnType);
    }

    RpcRequest request =
        RpcRequest.builder()
            .service(context.getService().getName())
            .method(methodName)
            .args(buildRequestArgs(method.getParameterTypes(), args))
            .build();

    List<Filter<?>> filters = context.getFilters();

    Optional<RpcResponse> response = Optional.empty();

    boolean intercepted = false;
    for (Filter<?> filter : filters) {
      Optional<? extends RpcResponse<?>> interceptedResponse = filter.preFilter(request);
      if (interceptedResponse.isPresent()) {
        response = Optional.of(interceptedResponse.get());
        intercepted = true;
        break;
      }
    }

    if (!intercepted) {
      response = httpInvoker.execute(request, this::buildHttpRequest);
      for (Filter<?> filter : filters) {
        if (response.isPresent()) {
          response = filter.postFilter(request, response.get());
        }
      }
    }

    if (response.isPresent()) {
      if (response.get().isSuccess()) {
        return GenericUtil.buildValue(
            returnType, method.getGenericReturnType(), response.get().getData());
      } else if (Objects.nonNull(response.get().getError())) {
        throw RpcExceptions.newTechErr("doInvoke fail", response.get().getError());
      }
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
        .url(
            chooseProvider()
                .map(Instance::buildUri)
                .orElseThrow(() -> new RuntimeException("find no providers")))
        .post(RequestBody.create(toJsonOrEmpty(request), JSON))
        .build();
  }

  private Optional<Instance> chooseProvider() {
    return context.getLoadBalancer().choose(context.getRouter().route(context.getProviders()));
  }
}
