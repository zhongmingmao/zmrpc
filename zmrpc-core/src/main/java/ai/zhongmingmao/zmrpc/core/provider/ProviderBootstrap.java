package ai.zhongmingmao.zmrpc.core.provider;

import ai.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import ai.zhongmingmao.zmrpc.core.api.RegistryCenter;
import ai.zhongmingmao.zmrpc.core.api.RpcRequest;
import ai.zhongmingmao.zmrpc.core.api.RpcResponse;
import ai.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import ai.zhongmingmao.zmrpc.core.utils.InstanceUtils;
import ai.zhongmingmao.zmrpc.core.utils.MethodUtils;
import ai.zhongmingmao.zmrpc.core.utils.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
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

  String instance;

  @Value("${server.port}")
  int port;

  // register services to registry center after spring application context is ready
  public void start() {
    skeleton.keySet().forEach(this::registerService);
  }

  @PostConstruct
  public void init() throws UnknownHostException {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.forEach((name, bean) -> System.out.println(name));
    providers
        .values()
        .forEach(
            bean -> {
              for (Class<?> serviceInterface : bean.getClass().getInterfaces()) {
                for (Method method : serviceInterface.getMethods()) {
                  if (!MethodUtils.checkLocalMethod(method)) {
                    createProvider(serviceInterface, bean, method);
                  }
                }
              }
            });

    instance = InstanceUtils.buildInstance(InetAddress.getLocalHost().getHostAddress(), port);
  }

  @PreDestroy
  public void stop() {
    skeleton.keySet().forEach(this::unregisterService);
  }

  private void registerService(String service) {
    applicationContext.getBean(RegistryCenter.class).register(service, instance);
  }

  private void unregisterService(String service) {
    applicationContext.getBean(RegistryCenter.class).unregister(service, instance);
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
      Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
      Object result = method.invoke(providerMeta.getServiceImpl(), args);
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

  private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
    if (args == null || args.length == 0) {
      return args;
    }

    Object[] targetArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      targetArgs[i] = TypeUtils.cast(args[i], parameterTypes[i]);
    }
    return targetArgs;
  }
}
