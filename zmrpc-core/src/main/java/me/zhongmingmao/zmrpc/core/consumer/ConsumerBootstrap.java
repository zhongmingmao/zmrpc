package me.zhongmingmao.zmrpc.core.consumer;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.annotation.ZmConsumer;
import me.zhongmingmao.zmrpc.core.api.LoadBalancer;
import me.zhongmingmao.zmrpc.core.api.Router;
import me.zhongmingmao.zmrpc.core.api.RpcContext;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

  @Setter ApplicationContext applicationContext;
  @Setter Environment environment;

  Map<String, Object> stub = new HashMap<>();

  // 主要目的 - 为 @ZmConsumer 字段赋值 - 动态生成代理类，模拟 HTTP 请求
  // 通过 ApplicationRunner 调用，此时 ApplicationContext 已完全就绪
  public void start() {

    Router router = applicationContext.getBean(Router.class);
    LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
    RpcContext rpcContext = RpcContext.builder().router(router).loadBalancer(loadBalancer).build();

    String urls = environment.getProperty("zmrpc.providers", "");
    if (Strings.isEmpty(urls)) {
      System.err.println("zmrpc.providers is empty.");
    }
    String[] providers = urls.split(",");

    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {
      Object bean = applicationContext.getBean(name);

      // bean.getClass() 是被 Spring 增强过的子类
      List<Field> fields = findAnnotatedField(bean.getClass());

      fields.forEach(
          field -> {
            System.out.println(" ===> " + field.getName());
            try {
              Class<?> service = field.getType();
              String serviceName = service.getCanonicalName();
              if (!stub.containsKey(serviceName)) {
                Object consumer = createConsumer(service, rpcContext, List.of(providers)); // 生成动态代理
                field.setAccessible(true);
                field.set(bean, consumer);
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  // service 为被 @ZmConsumer 修饰的字段的类型
  private Object createConsumer(Class<?> service, RpcContext rpcContext, List<String> providers) {
    return Proxy.newProxyInstance(
        service.getClassLoader(),
        new Class[] {service},
        new ZmInvocationHandler(service, rpcContext, providers));
  }

  private List<Field> findAnnotatedField(Class<?> klass) {
    List<Field> annotatedFields = new ArrayList<>();

    while (klass != null) {
      Field[] fields = klass.getDeclaredFields();
      for (Field field : fields) {
        if (field.isAnnotationPresent(ZmConsumer.class)) {
          annotatedFields.add(field);
        }
      }

      // 由于 bean.getClass() 是被 Spring 增强过的子类
      // 因此，需要不停地向上寻找 @ZmConsumer 修饰的字段
      klass = klass.getSuperclass();
    }

    return annotatedFields;
  }
}
