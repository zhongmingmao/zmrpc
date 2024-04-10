package io.zhongmingmao.zmrpc.core.consumer;

import com.google.common.collect.Lists;
import io.zhongmingmao.zmrpc.core.annotatation.ZmConsumer;
import io.zhongmingmao.zmrpc.core.api.context.RpcContext;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.LoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.router.Router;
import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  final Registry registry;

  public ConsumerBootstrap(Registry registry) {
    this.registry = registry;
  }

  public void buildProxyConsumers() {
    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {
      Object bean = applicationContext.getBean(name);
      List<Field> fields = findConsumerFields(bean.getClass());
      for (Field field : fields) {
        try {
          Object proxyConsumer = buildProxyConsumer(field.getType());
          field.setAccessible(true);
          field.set(bean, proxyConsumer);
          log.info(
              "buildProxyConsumer, {}#{}",
              field.getDeclaringClass().getCanonicalName(),
              field.getName());
        } catch (IllegalAccessException e) {
          log.error("buildProxyConsumer error", e);
        }
      }
    }
  }

  private List<Field> findConsumerFields(Class<?> klass) {
    // klass maybe proxied
    List<Field> fields = Lists.newArrayList();
    while (Objects.nonNull(klass)) {
      List<Field> consumerFields =
          Arrays.stream(klass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(ZmConsumer.class))
              .toList();
      fields.addAll(consumerFields);
      klass = klass.getSuperclass();
    }
    return fields;
  }

  private Object buildProxyConsumer(final Class<?> service) {
    ZmInvocationHandler handler =
        ZmInvocationHandler.builder().context(buildRpcContext(service)).build();
    registry.subscribe(service.getCanonicalName(), handler::refreshContext);
    return Proxy.newProxyInstance(service.getClassLoader(), new Class[] {service}, handler);
  }

  private RpcContext<String> buildRpcContext(final Class<?> service) {
    return RpcContext.<String>builder()
        .service(service.getCanonicalName())
        .providers(fetchProviders(service.getCanonicalName()))
        .router(applicationContext.getBean(Router.class))
        .loadBalancer(applicationContext.getBean(LoadBalancer.class))
        .build();
  }

  private List<String> fetchProviders(final String service) {
    List<String> instances = registry.fetchInstances(service);
    return instances.stream().map(ConsumerUtil::buildProvider).collect(Collectors.toList());
  }
}
