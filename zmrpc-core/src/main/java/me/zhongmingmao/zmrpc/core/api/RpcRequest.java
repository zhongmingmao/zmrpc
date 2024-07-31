package me.zhongmingmao.zmrpc.core.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcRequest {
  String service; // me.zhongmingmao.zmrpc.demo.api.UserService
  String method; // findById
  Object[] args; // 0
}
