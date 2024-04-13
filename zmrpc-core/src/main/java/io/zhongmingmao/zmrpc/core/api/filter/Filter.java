package io.zhongmingmao.zmrpc.core.api.filter;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;

import java.util.Optional;

public interface Filter<T> {

  Optional<RpcResponse<T>> preFilter(final RpcRequest request);

  Optional<RpcResponse<T>> postFilter(final RpcRequest request, final RpcResponse<T> response);
}
