package ai.zhongmingmao.zmrpc.core.provider;

import ai.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import ai.zhongmingmao.zmrpc.core.api.RpcRequest;
import ai.zhongmingmao.zmrpc.core.api.RpcResponse;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  ApplicationContext applicationContext;

  Map<String, Object> skeleton = Maps.newHashMap();

  @PostConstruct
  public void buildProviders() {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.forEach((name, bean) -> System.out.println(name));
    providers.values().forEach(bean -> skeleton.putIfAbsent(generateInterface(bean), bean));
  }

  private String generateInterface(Object bean) {
    return bean.getClass().getInterfaces()[0].getCanonicalName();
  }

  public RpcResponse invoke(RpcRequest request) {
    String methodName = request.getMethod();
    if ("toString".equals(methodName)
        || "getClass".equals(methodName)
        || "notify".equals(methodName)
        || "notifyAll".equals(methodName)
        || "wait".equals(methodName)
        || "hashCode".equals(methodName)
        || "equals".equals(methodName)) {
      return null;
    }

    Object bean = skeleton.get(request.getService());
    try {
      Method method = findMethod(bean, request.getMethod());
      Object result = method.invoke(bean, request.getArgs());
      return RpcResponse.builder().status(true).data(result).build();
    } catch (InvocationTargetException e) {
      return RpcResponse.builder()
          .status(false)
          .ex(new RuntimeException(e.getTargetException().getMessage()))
          .build();
    } catch (IllegalAccessException e) {
      return RpcResponse.builder().status(false).ex(new RuntimeException(e.getMessage())).build();
    }
  }

  private Method findMethod(Object bean, String methodName) {
    for (Method method : bean.getClass().getMethods()) {
      if (method.getName().equals(methodName)) {
        return method;
      }
    }
    return null;
  }
}
