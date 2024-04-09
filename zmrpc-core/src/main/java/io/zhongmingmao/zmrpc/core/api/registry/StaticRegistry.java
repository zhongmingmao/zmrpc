package io.zhongmingmao.zmrpc.core.api.registry;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaticRegistry implements Registry {

  List<String> providers;

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void register(String service, String instance) {}

  @Override
  public void unregister(String service, String instance) {}

  @Override
  public List<String> fetchInstances(String service) {
    return providers;
  }

  @Override
  public void subscribe() {}
}
