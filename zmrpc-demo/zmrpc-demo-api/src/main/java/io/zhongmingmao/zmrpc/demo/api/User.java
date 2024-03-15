package io.zhongmingmao.zmrpc.demo.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class User {
  Integer id;
  String name;
}
