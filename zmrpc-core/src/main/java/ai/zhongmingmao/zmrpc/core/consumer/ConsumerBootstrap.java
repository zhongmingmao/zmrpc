package ai.zhongmingmao.zmrpc.core.consumer;

import ai.zhongmingmao.zmrpc.core.annotation.ZmConsumer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware {

  ApplicationContext applicationContext;

  Map<String, Object> stub = Maps.newHashMap();

  public void start() {
    for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
      Object bean = applicationContext.getBean(beanDefinitionName);
      List<Field> fields = findAnnotationFields(bean.getClass());
      fields.forEach(
          field -> {
            try {
              Class<?> service = field.getType();
              String serviceName = field.getType().getCanonicalName();
              if (!stub.containsKey(serviceName)) {
                Object consumer = createConsumer(service);
                field.setAccessible(true);
                field.set(bean, consumer);
                stub.put(serviceName, consumer);
              }
            } catch (IllegalArgumentException | IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  private List<Field> findAnnotationFields(Class<?> beanClass) {
    List<Field> fields = Lists.newArrayList();

    // bean is enhanced by cglib
    while (beanClass != null) {
      for (Field field : beanClass.getDeclaredFields()) {
        if (field.isAnnotationPresent(ZmConsumer.class)) {
          fields.add(field);
        }
      }
      beanClass = beanClass.getSuperclass();
    }

    return fields;
  }

  private Object createConsumer(Class<?> service) {
    return Proxy.newProxyInstance(
        service.getClassLoader(), new Class[] {service}, new ZmInvocationHandler(service));
  }
}
