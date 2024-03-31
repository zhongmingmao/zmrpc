package io.zhongmingmao.zmrpc.demo.consumer;

import io.zhongmingmao.zmrpc.core.consumer.ConsumerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsumerConfiguration.class)
public class DemoConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoConsumerApplication.class, args);
  }
}
