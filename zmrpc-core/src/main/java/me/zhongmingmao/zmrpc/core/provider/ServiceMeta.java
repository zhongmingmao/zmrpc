package me.zhongmingmao.zmrpc.core.provider;

import com.alibaba.fastjson.JSON;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceMeta {

  String app;
  String namespace;
  String env;
  String name;
  Map<String, String> parameters;

  public String toZkPath() {
    return String.format("%s_%s_%s_%s", app, namespace, env, name);
  }

  public String toMetas() {
    return JSON.toJSONString(Optional.ofNullable(parameters).orElse(Collections.emptyMap()));
  }
}
