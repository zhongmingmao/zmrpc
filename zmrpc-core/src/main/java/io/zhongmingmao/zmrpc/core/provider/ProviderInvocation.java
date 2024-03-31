package io.zhongmingmao.zmrpc.core.provider;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderInvocation {
  Object provider;
  Method method;
}
