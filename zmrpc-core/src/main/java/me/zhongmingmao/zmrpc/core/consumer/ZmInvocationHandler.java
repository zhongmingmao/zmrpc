package me.zhongmingmao.zmrpc.core.consumer;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
import java.util.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.api.*;
import me.zhongmingmao.zmrpc.core.consumer.http.HttpInvoker;
import me.zhongmingmao.zmrpc.core.consumer.http.OkHttpInvoker;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import me.zhongmingmao.zmrpc.core.util.TypeUtils;
import org.jetbrains.annotations.Nullable;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;
  RpcContext rpcContext;
  List<InstanceMeta> providers;
  HttpInvoker httpInvoker;

  public ZmInvocationHandler(
      Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
    this.service = service;
    this.rpcContext = rpcContext;
    this.providers = providers;

    int timeout = Integer.parseInt(rpcContext.getParameters().getOrDefault("app.timeout", "1000"));
    httpInvoker = new OkHttpInvoker(timeout);
  }

  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 服务挡板
    if (MethodUtils.checkLocalMethod(method)) {
      // TBD，请求不发送到 Provider
      return null;
    }

    RpcRequest rpcRequest = new RpcRequest();
    rpcRequest.setService(service.getCanonicalName()); // service 为被 @ZmConsumer 修饰的字段的类型
    rpcRequest.setMethodSign(MethodUtils.methodSign(method)); // 计算方法签名
    rpcRequest.setArgs(args);

    // 重试策略 - 发生 SocketTimeoutException，则重新进行 LB
    int retries = Integer.parseInt(rpcContext.getParameters().getOrDefault("app.retries", "1"));
    while (retries-- > 0) {
      log.debug("===> retries: {}", retries);
      try {
        List<Filter> filters = rpcContext.getFilters();
        for (Filter filter : filters) {
          Object filterResult = filter.preFilter(rpcRequest);
          if (filterResult != null) { // cached or blocked
            log.debug(
                "{} ==> preFilter, filterResult: {}", filter.getClass().getName(), filterResult);
            return filterResult;
          }
        }

        // 重新进行 LB
        List<InstanceMeta> instances = rpcContext.getRouter().route(providers);
        InstanceMeta instance = rpcContext.getLoadBalancer().choose(instances);
        log.debug("select ==> " + instance.toUrl());
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());

        Object result = castResult(method, rpcResponse);
        for (Filter filter : filters) {
          filter.postFilter(rpcRequest, rpcResponse, result); // chain
        }

        return result;
      } catch (Exception e) {
        log.warn("invoke fail, cause: {}", e.getCause().getMessage());
        if (!(e.getCause() instanceof SocketTimeoutException)) {
          throw e;
        }
      }
    }

    return null;
  }

  @Nullable
  private static Object castResult(Method method, RpcResponse<?> rpcResponse) {
    if (rpcResponse.isStatus()) {
      return TypeUtils.castMethodResult(method, rpcResponse.getData());
    } else {
      Exception exception = rpcResponse.getEx();
      if (exception instanceof RpcException e) {
        throw e;
      }

      throw new RpcException(
          exception, RpcException.UnknownException); // 直接抛出，Provider 的异常能传递到 Consumer
    }
  }
}
