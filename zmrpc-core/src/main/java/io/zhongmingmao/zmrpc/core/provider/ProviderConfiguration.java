package io.zhongmingmao.zmrpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfiguration {

  @Bean
  public ProviderBootstrap providerBootstrap() {
    return new ProviderBootstrap();
  }
}
