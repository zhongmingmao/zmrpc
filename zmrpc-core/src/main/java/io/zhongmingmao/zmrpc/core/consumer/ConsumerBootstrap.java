package io.zhongmingmao.zmrpc.core.consumer;

import com.google.common.collect.Lists;
import io.zhongmingmao.zmrpc.core.annotatation.ZmConsumer;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

  @Setter ApplicationContext applicationContext;
  @Setter Environment environment;

  public void buildConsumers() {
    for (String name : applicationContext.getBeanDefinitionNames()) {
      Object bean = applicationContext.getBean(name);
      findAnnotatedFields(bean.getClass())
          .forEach(
              field -> {
                try {
                  Object consumer = buildProxyConsumer(field.getType());
                  field.setAccessible(true);
                  field.set(bean, consumer);
                  log.info(
                      "inject proxied consumer to {}#{}",
                      field.getDeclaringClass().getCanonicalName(),
                      field.getName());
                } catch (IllegalAccessException e) {
                  log.error("buildConsumers fail", e);
                }
              });
    }
  }

  private List<Field> findAnnotatedFields(Class<?> klass) {
    // since klass is proxied, so getDeclaredFields() will return empty list
    // get super classes to get fields
    List<Field> fields = Lists.newArrayList();
    while (Objects.nonNull(klass)) {
      fields.addAll(
          Arrays.stream(klass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(ZmConsumer.class))
              .toList());
      klass = klass.getSuperclass();
    }
    return fields;
  }

  private Object buildProxyConsumer(final Class<?> service) {
    return Proxy.newProxyInstance(
        service.getClassLoader(),
        new Class[] {service},
        ZmInvocationHandler.builder().environment(environment).service(service).build());
  }
}
