package io.zhongmingmao.zmrpc.core.provider;

import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import io.zhongmingmao.zmrpc.core.api.registry.ZookeeperRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ProviderConfiguration {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Registry registry(final Environment environment) {
    return new ZookeeperRegistry(environment);
  }

  @Bean
  public ProviderBootstrap providerBootstrap(final Registry registry) {
    return new ProviderBootstrap(registry);
  }

  @Bean
  public ProviderInvoker providerInvoker(final ProviderBootstrap bootstrap) {
    return new ProviderInvoker(bootstrap);
  }

  @Bean
  public ApplicationRunner register(final ProviderBootstrap providerBootstrap) {
    return args -> providerBootstrap.register();
  }
}
