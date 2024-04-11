package io.zhongmingmao.zmrpc.core.api.registry.meta;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Service implements RegistryMeta {

  String name;

  public static Service of(final String name) {
    return Service.builder().name(name).build();
  }

  @Override
  public String buildRegistryPath() {
    return "/" + name;
  }
}
