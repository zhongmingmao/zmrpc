package io.zhongmingmao.zmrpc.core.provider;

import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.core.api.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
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
  final Map<String, Object> skeleton = new HashMap<>();

  @PostConstruct
  public void init() {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers
        .values()
        .forEach(
            provider ->
                Arrays.stream(provider.getClass().getInterfaces())
                    .findFirst()
                    .ifPresent(i -> this.skeleton.put(i.getCanonicalName(), provider)));
  }

  public RpcResponse invoke(final RpcRequest request) {
    try {
      Object bean = skeleton.get(request.getService());
      Optional<Method> method = findMethod(bean.getClass(), request.getMethod());
      if (method.isEmpty()) {
        return RpcResponse.builder().success(false).data("method not found").build();
      }
      return RpcResponse.builder()
          .success(true)
          .data(method.get().invoke(bean, request.getArgs()))
          .build();
    } catch (InvocationTargetException | IllegalAccessException e) {
      log.error("invoke fail, request: " + request, e);
      return RpcResponse.builder().success(false).data(e.getMessage()).build();
    }
  }

  private Optional<Method> findMethod(final Class<?> klass, final String method) {
    return Arrays.stream(klass.getMethods())
        .filter(m -> Objects.equals(m.getName(), method))
        .findFirst();
  }
}
