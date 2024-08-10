package me.zhongmingmao.zmrpc.core.api;

public interface Filter {

  // 过滤请求，缓存或者拦截
  Object preFilter(RpcRequest request);

  // 对响应进行加工
  void postFilter(RpcRequest request, RpcResponse response, Object result);

  //  Filter next();

  Filter DEFAULT =
      new Filter() {
        @Override
        public Object preFilter(RpcRequest request) {
          return null; // no cache or block
        }

        @Override
        public void postFilter(RpcRequest request, RpcResponse response, Object result) {}
      };
}
