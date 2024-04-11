package io.zhongmingmao.zmrpc.core.consumer.transport.http;

import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.util.HttpUtil;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Slf4j
@Data
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OkHttpInvoker implements HttpInvoker<Request> {

  OkHttpClient client = HttpUtil.buildClient();

  @Override
  public Optional<RpcResponse> execute(RpcRequest request, Function<RpcRequest, Request> builder) {
    return HttpUtil.execute(client, builder.apply(request), RpcResponse.class);
  }
}
