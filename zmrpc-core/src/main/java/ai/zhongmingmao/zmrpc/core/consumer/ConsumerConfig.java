package ai.zhongmingmao.zmrpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

  @Bean
  ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  @Bean
  @Order(Integer.MIN_VALUE)
  ApplicationRunner bootstrap(@Autowired ConsumerBootstrap bootstrap) {
    return args -> {
      System.out.println("consumer bootstrap...");
      bootstrap.start();
    };
  }
}
