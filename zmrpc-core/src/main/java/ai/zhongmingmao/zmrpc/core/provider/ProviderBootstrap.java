package ai.zhongmingmao.zmrpc.core.provider;

import ai.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import ai.zhongmingmao.zmrpc.core.api.RpcRequest;
import ai.zhongmingmao.zmrpc.core.api.RpcResponse;
import ai.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import ai.zhongmingmao.zmrpc.core.utils.MethodUtils;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  ApplicationContext applicationContext;

  MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

  @PostConstruct
  public void start() {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.forEach((name, bean) -> System.out.println(name));
    providers
        .values()
        .forEach(
            bean -> {
              Class<?> serviceInterface = bean.getClass().getInterfaces()[0];
              for (Method method : serviceInterface.getMethods()) {
                if (!MethodUtils.checkLocalMethod(method)) {
                  createProvider(serviceInterface, bean, method);
                }
              }
            });
  }

  private void createProvider(Class<?> serviceInterface, Object bean, Method method) {
    ProviderMeta providerMeta =
        ProviderMeta.builder()
            .method(method)
            .serviceImpl(bean)
            .methodSign(MethodUtils.methodSign(method))
            .build();
    System.out.println("createProvider, providerMeta: " + providerMeta);
    skeleton.add(serviceInterface.getCanonicalName(), providerMeta);
  }

  public RpcResponse invoke(RpcRequest request) {
    String methodSign = request.getMethodSign();
    List<ProviderMeta> providerMetas = skeleton.get(request.getService());
    try {
      ProviderMeta providerMeta = findProviderMeta(providerMetas, methodSign);
      Method method = providerMeta.getMethod();
      Object result = method.invoke(providerMeta.getServiceImpl(), request.getArgs());
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

  private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
    return providerMetas.stream()
        .filter(providerMeta -> Objects.equals(methodSign, providerMeta.getMethodSign()))
        .findFirst()
        .orElse(null);
  }
}
