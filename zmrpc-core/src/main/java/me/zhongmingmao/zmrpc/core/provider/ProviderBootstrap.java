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
    if (request.getMethod().equals("toString")
        || request.getMethod().equals("hashCode")
        || request.getMethod().equals("equals")) {
      return null;
    }

    RpcResponse response = new RpcResponse();

    Object provider = skeleton.get(request.getService());
    try {
      Method method = findMethod(provider.getClass(), request.getMethod());
      Object result = method.invoke(provider, request.getArgs());

      response.setStatus(true);
      response.setData(result);
      return response;
      // 简化异常信息，无需将 Provider 完整堆栈返回给 Consumer
    } catch (InvocationTargetException e) {
      response.setEx(new RuntimeException(e.getTargetException().getMessage()));
    } catch (IllegalAccessException e) {
      response.setEx(new RuntimeException(e.getMessage()));
    }

    return response;
  }

  private Method findMethod(Class<?> klass, String methodName) {
    Method[] methods = klass.getMethods();
    for (Method method : methods) {
      if (Objects.equals(method.getName(), methodName)) {
        return method;
      }
    }
    return null;
  }
}
