package me.zhongmingmao.zmrpc.core.filter;

import lombok.SneakyThrows;
import me.zhongmingmao.zmrpc.core.api.Filter;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.util.MockUtils;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MockFilter implements Filter {

  @SneakyThrows
  @Override
  public Object preFilter(RpcRequest request) {
    Class<?> service = Class.forName(request.getService());
    Method method = findMethod(service, request.getMethodSign());
    return MockUtils.mock(method.getReturnType());
  }

  private Method findMethod(Class<?> service, String methodSign) {
    return Arrays.stream(service.getMethods())
        .filter(method -> !MethodUtils.checkLocalMethod(method))
        .filter(method -> Objects.equals(methodSign, MethodUtils.methodSign(method)))
        .findFirst()
        .orElse(null);
  }

  @Override
  public void postFilter(RpcRequest request, RpcResponse response, Object result) {}
}
