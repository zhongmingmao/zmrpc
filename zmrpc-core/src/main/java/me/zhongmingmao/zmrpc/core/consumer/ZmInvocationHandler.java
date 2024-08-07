package me.zhongmingmao.zmrpc.core.consumer;

import java.lang.reflect.*;
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

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;
  RpcContext rpcContext;
  List<InstanceMeta> providers;
  HttpInvoker httpInvoker = new OkHttpInvoker();

  public ZmInvocationHandler(
      Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
    this.service = service;
    this.rpcContext = rpcContext;
    this.providers = providers;
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

    List<InstanceMeta> instances = rpcContext.getRouter().route(providers);
    InstanceMeta instance = rpcContext.getLoadBalancer().choose(instances);
    log.debug("select ==> " + instance.toUrl());
    RpcResponse<?> rpcResponse = httpInvoker.post(request, instance.toUrl());

    if (rpcResponse.isStatus()) {
      return TypeUtils.castMethodResult(method, rpcResponse.getData());
    } else {
      // 异常
      Exception ex = rpcResponse.getEx();
      // ex.printStackTrace();
      throw new RuntimeException(ex); // 直接抛出，Provider 的异常能传递到 Consumer
    }
  }
}
