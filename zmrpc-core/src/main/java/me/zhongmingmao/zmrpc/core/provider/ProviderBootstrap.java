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
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  RegistryCenter registryCenter;

  @Getter MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
  InstanceMeta instance;

  @Value("${server.port}")
  String port;

  @Value("${app.id}")
  String app;

  @Value("${app.namespace}")
  String namespace;

  @Value("${app.env}")
  String env;

  @PostConstruct // init-method
  public void init() {
    registryCenter = applicationContext.getBean(RegistryCenter.class);

    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.values().forEach(this::genInterface);

    skeleton.forEach((service, provider) -> log.info(service + " -> " + provider));
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
    log.info("==> unregister all service");
    skeleton.keySet().forEach(this::unregisterService);

    // 关闭注册中心
    registryCenter.stop();
  }

  private ServiceMeta buildServiceMeta(String service) {
    return ServiceMeta.builder().app(app).namespace(namespace).env(env).name(service).build();
  }

  private void registerService(String service) {
    registryCenter.register(
        buildServiceMeta(service), instance); // Spring 上下文尚未完全就绪，服务已经注册上去了，可能会被发现
  }

  private void unregisterService(String service) {
    registryCenter.unregister(buildServiceMeta(service), instance);
  }

  private void genInterface(Object impl) {
    Class<?>[] interfaces = impl.getClass().getInterfaces();
    Arrays.stream(interfaces)
        .forEach(
            service -> {
              Method[] methods = service.getMethods();
              for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                  continue;
                }
                createProvider(service, impl, method);
              }
            });
  }

  private void createProvider(Class<?> service, Object impl, Method method) {
    ProviderMeta providerMeta =
        ProviderMeta.builder()
            .method(method)
            .serviceImpl(impl)
            .methodSign(MethodUtils.methodSign(method))
            .build();
    log.info("createProvider, " + providerMeta);
    skeleton.add(service.getCanonicalName(), providerMeta);
  }
}
