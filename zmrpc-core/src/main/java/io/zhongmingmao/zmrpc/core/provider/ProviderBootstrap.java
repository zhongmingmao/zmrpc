package io.zhongmingmao.zmrpc.core.provider;

import static io.zhongmingmao.zmrpc.core.provider.ProviderUtil.buildRequestArgTypes;
import static io.zhongmingmao.zmrpc.core.provider.ProviderUtil.buildRequestArgValues;

import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.util.MethodUtil;
import io.zhongmingmao.zmrpc.core.util.SignUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
public class ProviderBootstrap implements ApplicationContextAware, EnvironmentAware {

  @Setter ApplicationContext applicationContext;
  @Setter Environment environment;

  final Registry registry;
  final Map<String, Object> skeleton = Maps.newHashMap();
  final Map<String, ProviderInvocation> invocations = Maps.newHashMap();

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
    skeleton.keySet().forEach(this::register);
  }

  @PreDestroy
  public void unregister() {
    skeleton.keySet().forEach(this::unregister);
  }

  private void register(final String service) {
    buildInstance().ifPresent(instance -> registry.register(service, instance));
  }

  private void unregister(final String service) {
    buildInstance().ifPresent(instance -> registry.unregister(service, instance));
  }

  private Optional<String> buildInstance() {
    try {
      String address = InetAddress.getLocalHost().getHostAddress();
      String port = environment.getProperty("server.port");
      String instance = String.join("_", address, port);
      return Optional.of(instance);
    } catch (UnknownHostException e) {
      log.error("buildInstance fail", e);
      return Optional.empty();
    }
  }

  public RpcResponse<?> invoke(final RpcRequest request) {
    try {
      String sign = SignUtil.buildRequestSign(request);
      tryRegisterInvocation(request, sign);
      return doInvoke(invocations.get(sign), request.getArgs());
    } catch (Exception e) {
      log.error("provider invoke error, request: " + request, e);
      return RpcResponse.builder()
          .success(false)
          .error("provider invoke error, " + e.getMessage())
          .build();
    }
  }

  private void tryRegisterInvocation(final RpcRequest request, final String sign)
      throws NoSuchMethodException {
    if (invocations.containsKey(sign)) {
      return;
    }

    String service = request.getService();
    if (!skeleton.containsKey(service)) {
      log.error("{} is not registered yet", service);
      return;
    }

    String methodName = request.getMethod();
    if (MethodUtil.isReservedMethod(methodName)) {
      log.warn("{} is reserved, unable to be invoked", methodName);
      return;
    }

    Object provider = skeleton.get(service);
    Method method =
        provider.getClass().getMethod(methodName, buildRequestArgTypes(request.getArgs()));

    invocations.put(sign, ProviderInvocation.builder().provider(provider).method(method).build());
  }

  private RpcResponse<?> doInvoke(final ProviderInvocation invocation, final RpcRequestArg[] args)
      throws InvocationTargetException, IllegalAccessException {
    if (Objects.isNull(invocation)) {
      return RpcResponse.builder().success(false).error("invocation is null").build();
    }

    Object provider = invocation.getProvider();
    Method method = invocation.getMethod();
    Object data =
        method.invoke(
            provider,
            buildRequestArgValues(
                method.getParameterTypes(), method.getGenericParameterTypes(), args));
    return RpcResponse.builder().success(true).data(data).build();
  }
}
