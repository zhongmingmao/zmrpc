package io.zhongmingmao.zmrpc.core.provider;

import static io.zhongmingmao.zmrpc.core.provider.ProviderUtil.buildRequestArgTypes;
import static io.zhongmingmao.zmrpc.core.provider.ProviderUtil.buildRequestArgValues;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.util.MethodUtil;
import io.zhongmingmao.zmrpc.core.util.SignUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProviderInvoker {

  Map<String, Object> skeleton;
  Map<String, ProviderInvocation> invocations;

  public ProviderInvoker(final ProviderBootstrap bootstrap) {
    skeleton = bootstrap.getSkeleton();
    invocations = bootstrap.getInvocations();
  }

  public RpcResponse<?> invoke(final RpcRequest request) {
    try {
      String sign = SignUtil.buildRequestSign(request);
      tryRegisterInvocation(request, sign);
      return doInvoke(invocations.get(sign), request.getArgs());
    } catch (Exception e) {
      log.error("provider invoke error, request: " + request, e);
      return RpcResponse.builder()
          .success(false)
          .error("provider invoke error, " + e.getMessage())
          .build();
    }
  }

  private void tryRegisterInvocation(final RpcRequest request, final String sign)
      throws NoSuchMethodException {
    if (invocations.containsKey(sign)) {
      return;
    }

    String service = request.getService();
    if (!skeleton.containsKey(service)) {
      log.error("{} is not registered yet", service);
      return;
    }

    String methodName = request.getMethod();
    if (MethodUtil.isReservedMethod(methodName)) {
      log.warn("{} is reserved, unable to be invoked", methodName);
      return;
    }

    Object provider = skeleton.get(service);
    Method method =
        provider.getClass().getMethod(methodName, buildRequestArgTypes(request.getArgs()));

    invocations.put(sign, ProviderInvocation.builder().provider(provider).method(method).build());
  }

  private RpcResponse<?> doInvoke(final ProviderInvocation invocation, final RpcRequestArg[] args)
      throws InvocationTargetException, IllegalAccessException {
    if (Objects.isNull(invocation)) {
      return RpcResponse.builder().success(false).error("invocation is null").build();
    }

    Object provider = invocation.getProvider();
    Method method = invocation.getMethod();
    Object data =
        method.invoke(
            provider,
            buildRequestArgValues(
                method.getParameterTypes(), method.getGenericParameterTypes(), args));
    return RpcResponse.builder().success(true).data(data).build();
  }
}
