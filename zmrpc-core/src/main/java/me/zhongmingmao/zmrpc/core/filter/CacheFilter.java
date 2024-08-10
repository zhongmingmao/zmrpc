package me.zhongmingmao.zmrpc.core.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.zhongmingmao.zmrpc.core.api.Filter;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;

public class CacheFilter implements Filter {

  // TODO Guava
  static Map<String, Object> cache = new ConcurrentHashMap<>();

  @Override
  public Object preFilter(RpcRequest request) {
    return cache.get(request.toString());
  }

  @Override
  public void postFilter(RpcRequest request, RpcResponse response, Object result) {
    cache.putIfAbsent(request.toString(), result);
  }
}
