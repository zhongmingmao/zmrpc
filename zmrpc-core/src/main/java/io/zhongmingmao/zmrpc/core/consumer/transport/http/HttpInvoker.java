package io.zhongmingmao.zmrpc.core.consumer.transport.http;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface HttpInvoker<Req> {
  Optional<RpcResponse> execute(final RpcRequest request, final Function<RpcRequest, Req> builder);
}
