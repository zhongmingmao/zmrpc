package me.zhongmingmao.zmrpc.core.consumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.annotation.ZmConsumer;
import me.zhongmingmao.zmrpc.core.api.LoadBalancer;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.api.Router;
import me.zhongmingmao.zmrpc.core.api.RpcContext;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  Map<String, Object> stub = new HashMap<>();

  // 主要目的 - 为 @ZmConsumer 字段赋值 - 动态生成代理类，模拟 HTTP 请求
  // 通过 ApplicationRunner 调用，此时 ApplicationContext 已完全就绪
  public void start() {

    Router router = applicationContext.getBean(Router.class);
    LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
    RpcContext rpcContext = RpcContext.builder().router(router).loadBalancer(loadBalancer).build();

    RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {
      Object bean = applicationContext.getBean(name);

      // bean.getClass() 是被 Spring 增强过的子类
      List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), ZmConsumer.class);

      fields.forEach(
          field -> {
            System.out.println(" ===> " + field.getName());
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
              throw new RuntimeException(e);
            }
          });
    }
  }

  private Object createConsumerFromRegistry(
      Class<?> service, RpcContext rpcContext, RegistryCenter registryCenter) {
    String serviceName = service.getCanonicalName();

    List<String> providers = buildProviders(registryCenter.fetchAll(serviceName));
    System.out.println("===> providers: " + providers);

    // 注册监听器，监听到变化后，修改 Provider 列表
    registryCenter.subscribe(
        serviceName,
        event -> {
          List<String> changedNodes = event.getData(); // 获取传递出来的事件里面的数据
          providers.clear();
          List<String> changedProviders = buildProviders(changedNodes);
          System.out.println("===> changedProviders: " + changedProviders);
          providers.addAll(changedProviders);
        });

    return createConsumer(service, rpcContext, providers);
  }

  private List<String> buildProviders(List<String> nodes) {
    return nodes.stream()
        .map(node -> "http://" + node.replace('_', ':') + "/")
        .collect(Collectors.toList());
  }

  // service 为被 @ZmConsumer 修饰的字段的类型
  private Object createConsumer(Class<?> service, RpcContext rpcContext, List<String> providers) {
    return Proxy.newProxyInstance(
        service.getClassLoader(),
        new Class[] {service},
        new ZmInvocationHandler(service, rpcContext, providers));
  }
}
