package ai.zhongmingmao.zmrpc.core.meta;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderMeta {
  Method method;
  String methodSign;
  Object serviceImpl;
}
