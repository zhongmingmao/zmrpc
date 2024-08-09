package me.zhongmingmao.zmrpc.core.meta;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderMeta {
  Method method;
  String methodSign;
  Object serviceImpl;
}
