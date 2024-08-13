package me.zhongmingmao.zmrpc.core.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.annotation.ZmConsumer;
import me.zhongmingmao.zmrpc.core.api.*;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.provider.ServiceMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  @Value("${app.id}")
  String app;

  @Value("${app.namespace}")
  String namespace;

  @Value("${app.env}")
  String env;

  @Value("${app.retries}")
  int retries;

  @Value("${app.timeout}")
  int timeout;

  Map<String, Object> stub = new HashMap<>();

  // 主要目的 - 为 @ZmConsumer 字段赋值 - 动态生成代理类，模拟 HTTP 请求
  // 通过 ApplicationRunner 调用，此时 ApplicationContext 已完全就绪
  public void start() {
    List<Filter> filters =
        applicationContext.getBeansOfType(Filter.class).values().stream().toList();
    Router<InstanceMeta> router = applicationContext.getBean(Router.class);
    LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
    Map<String, String> parameters = new HashMap<>();
    parameters.put("app.retries", String.valueOf(retries));
    parameters.put("app.timeout", String.valueOf(timeout));

    RpcContext rpcContext =
        RpcContext.builder()
            .filters(filters)
            .router(router)
            .loadBalancer(loadBalancer)
            .parameters(parameters)
            .build();

    RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {
      Object bean = applicationContext.getBean(name);

      // bean.getClass() 是被 Spring 增强过的子类
      List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), ZmConsumer.class);

      fields.forEach(
          field -> {
            log.info(" ===> " + field.getName());
            try {
              Class<?> service = field.getType();
              String serviceName = service.getCanonicalName();
              if (!stub.containsKey(serviceName)) {
                Object consumer =
                    createConsumerFromRegistry(service, rpcContext, registryCenter); // 生成动态代理
                field.setAccessible(true);
                field.set(bean, consumer);
                stub.put(serviceName, consumer);
              }
            } catch (Exception e) {
              throw new RpcException(e, RpcException.ReflectException);
            }
          });
    }
  }

  private Object createConsumerFromRegistry(
      Class<?> service, RpcContext rpcContext, RegistryCenter registryCenter) {
    ServiceMeta serviceMeta = buildServiceMeta(service.getCanonicalName());

    List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
    log.info("===> providers: " + providers);

    // 注册监听器，监听到变化后，修改 Provider 列表
    registryCenter.subscribe(
        serviceMeta,
        event -> {
          List<InstanceMeta> changedNodes = event.getData(); // 获取传递出来的事件里面的数据
          providers.clear();
          log.info("===> changedProviders: " + changedNodes);
          providers.addAll(changedNodes);
        });

    return createConsumer(service, rpcContext, providers);
  }

  // service 为被 @ZmConsumer 修饰的字段的类型
  private Object createConsumer(
      Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
    return Proxy.newProxyInstance(
        service.getClassLoader(),
        new Class[] {service},
        new ZmInvocationHandler(service, rpcContext, providers));
  }

  private ServiceMeta buildServiceMeta(String service) {
    return ServiceMeta.builder().app(app).namespace(namespace).env(env).name(service).build();
  }
}
