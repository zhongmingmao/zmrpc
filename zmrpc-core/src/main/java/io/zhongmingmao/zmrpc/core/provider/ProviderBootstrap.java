package io.zhongmingmao.zmrpc.core.provider;

import static io.zhongmingmao.zmrpc.core.util.RpcUtil.buildArgs;
import static io.zhongmingmao.zmrpc.core.util.RpcUtil.buildTypes;

import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.constant.RpcConstant;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;
  final Map<String, Object> skeleton = Maps.newHashMap();

  @PostConstruct
  public void init() {
    // beans have not been fully initialized
    applicationContext
        .getBeansWithAnnotation(ZmProvider.class)
        .values()
        .forEach(
            provider ->
                // register the first interface of the provider
                Arrays.stream(provider.getClass().getInterfaces())
                    .findFirst()
                    .ifPresent(
                        i -> {
                          skeleton.put(i.getCanonicalName(), provider);
                          log.info(
                              "register provider to skeleton, interface: {}, provider: {}",
                              i.getCanonicalName(),
                              provider.getClass().getCanonicalName());
                        }));
  }

  public RpcResponse<?> invoke(final RpcRequest request) {
    String methodName = request.getMethod();
    if (RpcConstant.PROHIBITED_METHODS.contains(methodName)) {
      return RpcResponse.builder().success(false).error(methodName + " is prohibited").build();
    }

    try {
      Object provider = skeleton.get(request.getService());
      Method method = provider.getClass().getMethod(methodName, buildTypes(request.getArgs()));
      return RpcResponse.builder()
          .success(true)
          .data(method.invoke(provider, buildArgs(request.getArgs())))
          .build();
    } catch (Exception e) {
      log.error("invoke fail, request: " + request, e);
      return RpcResponse.builder()
          .success(false)
          .error("provider invoke fail, " + e.getMessage())
          .build();
    }
  }
}
