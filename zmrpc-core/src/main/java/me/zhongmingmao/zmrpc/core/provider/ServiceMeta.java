package me.zhongmingmao.zmrpc.core.provider;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceMeta {

  String app;
  String namespace;
  String env;
  String name;

  public String toZkPath() {
    return String.format("%s_%s_%s_%s", app, namespace, env, name);
  }
}
