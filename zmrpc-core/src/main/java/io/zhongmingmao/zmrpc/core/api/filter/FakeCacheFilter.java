package io.zhongmingmao.zmrpc.core.api.filter;

import com.google.common.collect.Maps;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.util.SignUtil;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

@Order()
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FakeCacheFilter<T> implements Filter<T> {

  Map<String, RpcResponse<T>> cache = Maps.newConcurrentMap();

  @Override
  public Optional<RpcResponse<T>> preFilter(RpcRequest request) {
    String sign = SignUtil.buildRequestSign(request);
    Optional<RpcResponse<T>> response = Optional.ofNullable(cache.get(sign));
    response.ifPresent(
        r -> log.debug("get a response from cache, sign: {}, response: {}", sign, r));
    return Optional.empty();
  }

  @Override
  public Optional<RpcResponse<T>> postFilter(RpcRequest request, RpcResponse<T> response) {
    cache.putIfAbsent(SignUtil.buildRequestSign(request), response);
    return Optional.ofNullable(response);
  }
}
