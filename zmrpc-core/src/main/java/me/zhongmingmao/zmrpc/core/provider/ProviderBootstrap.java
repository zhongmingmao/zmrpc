package me.zhongmingmao.zmrpc.core.provider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import me.zhongmingmao.zmrpc.core.util.TypeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  RegistryCenter registryCenter;

  MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
  String instance;

  @Value("${server.port}")
  String port;

  @PostConstruct // init-method
  public void init() {
    registryCenter = applicationContext.getBean(RegistryCenter.class);

    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.values().forEach(this::genInterface);

    skeleton.forEach((service, provider) -> System.out.println(service + " -> " + provider));
  }

  // 由 ApplicationRunner 触发，此时 ApplicationContext 已完全就绪，此时可以接收请求，即延迟注册
  @SneakyThrows
  public void start() {
    // 启动注册中心
    registryCenter.start();

    // 获取本机 IP
    String ip = InetAddress.getLocalHost().getHostAddress();
    instance = ip + "_" + port;

    // 注册服务
    skeleton.keySet().forEach(this::registerService);
  }

  @PreDestroy // @PreDestroy 在 @Bean(destroyMethod = "stop") 后执行
  public void stop() {
    // 反注册服务
    System.out.println("==> unregister all service");
    skeleton.keySet().forEach(this::unregisterService);

    // 关闭注册中心
    registryCenter.stop();
  }

  private void registerService(String service) {
    //    RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
    registryCenter.register(service, instance); // Spring 上下文尚未完全就绪，服务已经注册上去了，可能会被发现
  }

  private void unregisterService(String service) {
    //    RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
    registryCenter.unregister(service, instance);
  }

  private void genInterface(Object provider) {
    Class<?>[] interfaces = provider.getClass().getInterfaces();
    Arrays.stream(interfaces)
        .forEach(
            service -> {
              Method[] methods = service.getMethods();
              for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                  continue;
                }
                createProvider(service, provider, method);
              }
            });
  }

  private void createProvider(Class<?> service, Object provider, Method method) {
    ProviderMeta meta = new ProviderMeta();
    meta.setMethod(method);
    meta.setServiceImpl(provider);
    meta.setMethodSign(MethodUtils.methodSign(method));
    System.out.println("createProvider, " + meta);
    skeleton.add(service.getCanonicalName(), meta);
  }

  public RpcResponse invoke(RpcRequest request) {
    RpcResponse response = new RpcResponse();

    // 查找 Service 的方法列表
    List<ProviderMeta> metas = skeleton.get(request.getService());
    try {
      // 通过方法签名匹配
      ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());

      Method method = meta.getMethod();

      // 处理成方法预期的类型
      Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
      Object result = method.invoke(meta.getServiceImpl(), args);

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

  private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
    if (args == null || args.length == 0) {
      return args;
    }

    Object[] actuals = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
    }

    return actuals;
  }

  private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
    return metas.stream()
        .filter(providerMeta -> Objects.equals(methodSign, providerMeta.getMethodSign()))
        .findFirst()
        .orElse(null);
  }
}
