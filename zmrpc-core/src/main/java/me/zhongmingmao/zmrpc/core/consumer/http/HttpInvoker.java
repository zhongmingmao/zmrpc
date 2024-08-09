package me.zhongmingmao.zmrpc.core.consumer.http;

import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;

public interface HttpInvoker {
  RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
