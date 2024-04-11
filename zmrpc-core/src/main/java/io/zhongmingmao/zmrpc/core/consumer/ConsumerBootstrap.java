package io.zhongmingmao.zmrpc.core.consumer;

import static io.zhongmingmao.zmrpc.core.util.ReflectUtil.findConsumerFields;

import io.zhongmingmao.zmrpc.core.api.context.RpcContext;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.LoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.router.Router;
import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Service;
import io.zhongmingmao.zmrpc.core.consumer.transport.http.HttpInvoker;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  final Registry registry;
  final HttpInvoker<Request> httpInvoker;

  public ConsumerBootstrap(final Registry registry, final HttpInvoker<Request> httpInvoker) {
    this.registry = registry;
    this.httpInvoker = httpInvoker;
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

  private Object buildProxyConsumer(final Class<?> service) {
    ZmInvocationHandler handler =
        ZmInvocationHandler.builder()
            .context(buildRpcContext(service))
            .httpInvoker(httpInvoker)
            .build();
    registry.subscribe(Service.of(service.getCanonicalName()), handler::refreshContext);
    return Proxy.newProxyInstance(service.getClassLoader(), new Class[] {service}, handler);
  }

  private RpcContext buildRpcContext(final Class<?> service) {
    return RpcContext.builder()
        .service(Service.of(service.getCanonicalName()))
        .providers(fetchProviders(service.getCanonicalName()))
        .router(applicationContext.getBean(Router.class))
        .loadBalancer(applicationContext.getBean(LoadBalancer.class))
        .build();
  }

  private List<Instance> fetchProviders(final String service) {
    List<String> instances = registry.fetchInstances(Service.of(service));
    return instances.stream().map(ConsumerUtil::buildProvider).collect(Collectors.toList());
  }
}
