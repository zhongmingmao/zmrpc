package me.zhongmingmao.zmrpc.core.consumer;

import java.lang.reflect.*;
import java.util.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.*;
import me.zhongmingmao.zmrpc.core.consumer.http.HttpInvoker;
import me.zhongmingmao.zmrpc.core.consumer.http.OkHttpInvoker;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import me.zhongmingmao.zmrpc.core.util.TypeUtils;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;
  RpcContext rpcContext;
  List<String> providers;
  HttpInvoker httpInvoker = new OkHttpInvoker();

  public ZmInvocationHandler(Class<?> service, RpcContext rpcContext, List<String> providers) {
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

    List<String> urls = rpcContext.getRouter().route(providers);
    String url = (String) rpcContext.getLoadBalancer().choose(urls);
    System.out.println("select ==> " + url);
    RpcResponse<?> rpcResponse = httpInvoker.post(request, url);

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
