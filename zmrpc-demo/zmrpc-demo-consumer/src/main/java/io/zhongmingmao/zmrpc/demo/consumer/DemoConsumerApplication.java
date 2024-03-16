package io.zhongmingmao.zmrpc.demo.consumer;

import io.zhongmingmao.zmrpc.core.annotatation.ZmConsumer;
import io.zhongmingmao.zmrpc.core.consumer.ConsumerConfig;
import io.zhongmingmao.zmrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@Import(ConsumerConfig.class)
public class DemoConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoConsumerApplication.class, args);
  }

  @ZmConsumer UserService userService;

  @Bean
  public ApplicationRunner test() {
    return args -> {
      long id = 1 << 10;
      String name = "zhongmingmao";
      log.info("findUser: {}", userService.findUser());
      log.info("findUserByShort: {}", userService.findUserByShort((short) id));
      log.info("findUserByPrimitiveLong: {}", userService.findUserByPrimitiveLong(id));
      log.info("findUserByLong: {}", userService.findUserByLong(id));
      log.info("findUserByName: {}", userService.findUserByName(name));
      log.info("getId: {}", userService.getId(id));
      log.info("getName: {}", userService.getName(id));

      log.info("findUserByPrimitiveLong -1: {}", userService.findUserByPrimitiveLong(-1L));
      log.info("hashCode: {}", userService.hashCode());
      log.info("toString: {}", userService.toString());
    };
  }
}
