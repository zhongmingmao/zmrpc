package io.zhongmingmao.zmrpc.core.api.registry.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistryChangedEvent {
  String service;
  List<String> instances;
}
