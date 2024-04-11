package io.zhongmingmao.zmrpc.core.api.registry.meta;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Instance implements RegistryMeta {

  Service service;

  String schema;
  String host;
  Integer port;
  String context;

  public static Instance of(final String host, Integer port) {
    return Instance.builder().schema("http").host(host).port(port).context("rpc/invoke").build();
  }

  public static Instance of(final Service service, final String host, Integer port) {
    Instance instance = of(host, port);
    instance.setService(service);
    return instance;
  }

  @Override
  public String buildRegistryPath() {
    return String.join("/", service.buildRegistryPath(), identity());
  }

  public String buildUri() {
    return "%s://%s:%d/%s".formatted(schema, host, port, context);
  }

  private String identity() {
    return String.join("_", host, String.valueOf(port));
  }
}
