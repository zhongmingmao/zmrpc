package me.zhongmingmao.zmrpc.core.provider;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstanceMeta {

  String scheme;
  String host;
  Integer port;
  String context;

  boolean status; // online or offline
  Map<String, String> parameters;

  public String toZkPath() {
    return String.format("%s_%d", host, port);
  }

  public String toUrl() {
    return String.format("%s://%s:%d/%s", scheme, host, port, context);
  }

  public static InstanceMeta http(String host, Integer port) {
    return InstanceMeta.builder().scheme("http").host(host).port(port).context("").build();
  }
}
