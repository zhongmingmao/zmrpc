package io.zhongmingmao.zmrpc.demo.provider.controller;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.provider.ProviderInvoker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/rpc")
public class TestRpcController {

  ProviderInvoker invoker;

  @PostMapping("/invoke")
  public RpcResponse<?> invoke(final @RequestBody RpcRequest request) {
    return invoker.invoke(request);
  }
}
