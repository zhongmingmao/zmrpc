package ai.zhongmingmao.zmrpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {

  @Bean
  ProviderBootstrap providerBootstrap() {
    return new ProviderBootstrap();
  }
}
