package ai.zhongmingmao.zmrpc.core.provider;

import ai.zhongmingmao.zmrpc.core.api.RegistryCenter;
import ai.zhongmingmao.zmrpc.core.registry.ZkRegistryCenter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {

  @Bean
  public ProviderBootstrap providerBootstrap() {
    return new ProviderBootstrap();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  public RegistryCenter registryCenter() {
    return new ZkRegistryCenter();
  }

  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner bootstrap(ProviderBootstrap bootstrap) {
    return args -> {
      System.out.println("provider bootstrap...");
      bootstrap.start();
    };
  }
}
