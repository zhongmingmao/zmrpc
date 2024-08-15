package me.zhongmingmao.zmrpc.core.consumer;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.api.*;
import me.zhongmingmao.zmrpc.core.consumer.http.HttpInvoker;
import me.zhongmingmao.zmrpc.core.consumer.http.OkHttpInvoker;
import me.zhongmingmao.zmrpc.core.governance.SlidingTimeWindow;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import me.zhongmingmao.zmrpc.core.util.TypeUtils;
import org.jetbrains.annotations.Nullable;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;
  RpcContext rpcContext;
  HttpInvoker httpInvoker;

  final List<InstanceMeta> providers; // 全开区
  List<InstanceMeta> isolatedProviders = new ArrayList<>(); // 隔离区
  final List<InstanceMeta> halfOpenProviders = new ArrayList<>(); // 半开主要用于探活
  final Map<String, SlidingTimeWindow> windows = new HashMap<>(); // 滑动窗口记录实例异常次数

  public ZmInvocationHandler(
      Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
    this.service = service;
    this.rpcContext = rpcContext;
    this.providers = providers;

    int timeout = Integer.parseInt(rpcContext.getParameters().getOrDefault("app.timeout", "1000"));
    this.httpInvoker = new OkHttpInvoker(timeout);

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    // 定时拷贝实例：隔离区 -> 半开区（探活）
    executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, TimeUnit.SECONDS);
  }

  private void halfOpen() {
    log.debug("===> half open isolatedProviders: " + isolatedProviders);
    halfOpenProviders.clear();
    halfOpenProviders.addAll(isolatedProviders);
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
        boolean isHalfOpen = false;
        InstanceMeta instance;
        synchronized (halfOpenProviders) {
          if (halfOpenProviders.isEmpty()) {
            List<InstanceMeta> instances = rpcContext.getRouter().route(providers);
            instance = rpcContext.getLoadBalancer().choose(instances);
            log.debug("select ==> " + instance.toUrl());
          } else {
            // 探活 A1 -> A2 -> A3，逐个获取半开的实例来进行探活，一旦探活成功，回到全开状态
            instance = halfOpenProviders.remove(0);
            isHalfOpen = true;
            log.debug("===> check alive, instance: {}", instance);
          }
        }

        RpcResponse<?> rpcResponse;
        Object result;

        String url = instance.toUrl();
        try {
          rpcResponse = httpInvoker.post(rpcRequest, url);
          result = castResult(method, rpcResponse);
        } catch (Exception e) {
          // 故障的规则统计和隔离
          // 发生一次异常，记录一次，统计周期 30 秒
          synchronized (windows) {
            SlidingTimeWindow window = windows.get(url);
            if (window == null) {
              window = new SlidingTimeWindow();
              windows.put(url, window);
            }
            window.record(System.currentTimeMillis());
            log.debug("instance {} in window with {}", url, window.getSum());

            // 异常发生超过 N 次，则做故障隔离
            if (window.getSum() >= 10 && !isHalfOpen) {
              isolate(instance);
            }
          }

          throw e;
        }

        synchronized (providers) {
          if (!providers.contains(instance)) {
            // 此次请求为探活请求，探活成功，回到全开状态
            isolatedProviders.remove(instance);
            providers.add(instance);
            log.debug(
                "===> instance {} recovered, isolatedProviders={}, providers={}",
                instance,
                isolatedProviders,
                providers);
          }
        }

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

  // 隔离故障实例
  private void isolate(InstanceMeta instance) {
    log.debug("===> isolate instance: " + instance);
    providers.remove(instance);
    log.debug("===> providers: " + providers);
    isolatedProviders.add(instance);
    log.debug("===> isolatedProviders: " + isolatedProviders);
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
