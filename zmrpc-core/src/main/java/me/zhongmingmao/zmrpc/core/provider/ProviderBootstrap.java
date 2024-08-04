package me.zhongmingmao.zmrpc.core.provider;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import me.zhongmingmao.zmrpc.core.util.MethodUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

  @PostConstruct // init-method
  public void start() {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.values().forEach(this::genInterface);

    skeleton.forEach((service, provider) -> System.out.println(service + " -> " + provider));
  }

  private void genInterface(Object provider) {
    Class<?> service = provider.getClass().getInterfaces()[0];

    Method[] methods = service.getMethods();
    for (Method method : methods) {
      if (MethodUtils.checkLocalMethod(method)) {
        continue;
      }
      createProvider(service, provider, method);
    }
  }

  private void createProvider(Class<?> service, Object provider, Method method) {
    ProviderMeta meta = new ProviderMeta();
    meta.setMethod(method);
    meta.setServiceImpl(provider);
    meta.setMethodSign(MethodUtils.methodSign(method));
    System.out.println("createProvider, " + meta);
    skeleton.add(service.getCanonicalName(), meta);
  }

  public RpcResponse invoke(RpcRequest request) {
    RpcResponse response = new RpcResponse();

    // 查找 Service 的方法列表
    List<ProviderMeta> metas = skeleton.get(request.getService());
    try {
      // 通过方法签名匹配
      ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());

      Method method = meta.getMethod();
      Object result = method.invoke(meta.getServiceImpl(), request.getArgs());

      response.setStatus(true);
      response.setData(result);
      return response;
      // 简化异常信息，无需将 Provider 完整堆栈返回给 Consumer
    } catch (InvocationTargetException e) {
      response.setEx(new RuntimeException(e.getTargetException().getMessage()));
    } catch (IllegalAccessException e) {
      response.setEx(new RuntimeException(e.getMessage()));
    }

    return response;
  }

  private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
    return metas.stream()
        .filter(providerMeta -> Objects.equals(methodSign, providerMeta.getMethodSign()))
        .findFirst()
        .orElse(null);
  }
}
