package io.zhongmingmao.zmrpc.core.api.registry.event;

import io.zhongmingmao.zmrpc.core.api.registry.meta.Service;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistryChangedEvent {
  Service service;
  List<String> instances;
}
