package me.zhongmingmao.zmrpc.core.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.meta.ProviderMeta;
import me.zhongmingmao.zmrpc.core.util.TypeUtils;
import org.springframework.util.MultiValueMap;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderInvoker {

  MultiValueMap<String, ProviderMeta> skeleton;

  public ProviderInvoker(ProviderBootstrap providerBootstrap) {
    this.skeleton = providerBootstrap.getSkeleton();
  }

  public RpcResponse<Object> invoke(RpcRequest request) {
    RpcResponse<Object> response = new RpcResponse<>();

    // 查找 Service 的方法列表
    List<ProviderMeta> metas = skeleton.get(request.getService());
    try {
      // 通过方法签名匹配
      ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());

      Method method = meta.getMethod();

      // 处理成方法预期的类型
      Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
      Object result = method.invoke(meta.getServiceImpl(), args);

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

  private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
    if (args == null || args.length == 0) {
      return args;
    }

    Object[] actuals = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
    }

    return actuals;
  }

  private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
    return metas.stream()
        .filter(providerMeta -> Objects.equals(methodSign, providerMeta.getMethodSign()))
        .findFirst()
        .orElse(null);
  }
}
