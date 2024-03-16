package io.zhongmingmao.zmrpc.core.consumer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner buildConsumers(final ConsumerBootstrap bootstrap) {
    // ApplicationRunner#run: all beans are fully initialized
    return args -> bootstrap.buildConsumers();
  }
}
