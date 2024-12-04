package ai.zhongmingmao.zmrpc.core.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

public interface RegistryCenter {

  void start();

  void stop();

  void register(String service, String instance);

  void unregister(String service, String instance);

  List<String> findAll(String service);

  //  void subscribe();

  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  class StaticRegistryCenter implements RegistryCenter {

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
    public List<String> findAll(String service) {
      return providers;
    }
  }
}
