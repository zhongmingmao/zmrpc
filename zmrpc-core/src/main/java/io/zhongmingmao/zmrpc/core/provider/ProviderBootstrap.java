package io.zhongmingmao.zmrpc.core.provider;

import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.core.api.error.RpcExceptions;
import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Instance;
import io.zhongmingmao.zmrpc.core.api.registry.meta.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware, EnvironmentAware {

  @Setter ApplicationContext applicationContext;
  @Setter Environment environment;

  final Registry registry;
  @Getter final Map<String, Object> skeleton = Maps.newConcurrentMap();
  @Getter final Map<String, ProviderInvocation> invocations = Maps.newConcurrentMap();

  public ProviderBootstrap(Registry registry) {
    this.registry = registry;
  }

  @PostConstruct
  public void init() {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    beans.forEach(
        (name, provider) -> {
          Class<?>[] interfaces = provider.getClass().getInterfaces();
          for (Class<?> intf : interfaces) {
            String service = intf.getCanonicalName();
            if (skeleton.containsKey(service)) {
              log.error("duplicate provider, service: {}, provider: {}", service, provider);
              continue;
            }
            log.info("register provider to skeleton, service: {}, provider: {}", service, provider);
            skeleton.putIfAbsent(service, provider);
          }
        });
  }

  // ApplicationRunner -> Spring Context is ready -> register service to registry -> receive traffic
  public void register() {
    skeleton.keySet().stream().map(Service::of).forEach(this::register);
  }

  @PreDestroy
  public void unregister() {
    skeleton.keySet().stream().map(Service::of).forEach(this::unregister);
  }

  private void register(final Service service) {
    buildInstance(service).ifPresent(registry::register);
  }

  private void unregister(final Service service) {
    buildInstance(service).ifPresent(registry::unregister);
  }

  private Optional<Instance> buildInstance(final Service service) {
    try {
      String address = InetAddress.getLocalHost().getHostAddress();
      String port = environment.getProperty("server.port", "8080");
      return Optional.of(Instance.of(service, address, Integer.valueOf(port)));
    } catch (UnknownHostException e) {
      String message = "buildInstance fail, service: %s".formatted(service);
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }
}
