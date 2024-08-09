package me.zhongmingmao.zmrpc.core.provider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  RegistryCenter registryCenter;

  @Getter MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
  InstanceMeta instance;

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
    instance = InstanceMeta.http(ip, Integer.parseInt(port));

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
    registryCenter.register(service, instance); // Spring 上下文尚未完全就绪，服务已经注册上去了，可能会被发现
  }

  private void unregisterService(String service) {
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
}
