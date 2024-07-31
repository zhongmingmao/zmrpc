package me.zhongmingmao.zmrpc.core.provider;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  Map<String, Object> skeleton = new HashMap<>();

  @PostConstruct // init-method
  public void buildProviders() {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.values().forEach(this::genInterface);

    skeleton.forEach((service, provider) -> System.out.println(service + " -> " + provider));
  }

  private void genInterface(Object provider) {
    Class<?> service = provider.getClass().getInterfaces()[0];
    skeleton.put(service.getCanonicalName(), provider);
  }

  public RpcResponse invoke(RpcRequest request) {
    Object provider = skeleton.get(request.getService());

    try {
      Method[] methods = provider.getClass().getMethods();
      for (Method method : methods) {
        if (Objects.equals(method.getName(), request.getMethod())) {
          Object result = method.invoke(provider, request.getArgs());
          return new RpcResponse(true, result);
        }
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    return null;
  }
}
