package io.zhongmingmao.zmrpc.core.consumer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfiguration {

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner buildProxyConsumers(final ConsumerBootstrap bootstrap) {
    return args -> bootstrap.buildProxyConsumers();
  }
}
