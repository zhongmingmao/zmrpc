package me.zhongmingmao.zmrpc.demo.consumer;

import me.zhongmingmao.zmrpc.demo.provider.ZmrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class ZmrpcDemoConsumerApplicationTests {

  static ConfigurableApplicationContext context;

  @BeforeAll
  static void init() {
    context =
        SpringApplication.run(
            ZmrpcDemoProviderApplication.class,
            "--server.port=8084",
            "--logging.level.me.zhongmingmao.zmrpc=debug");
  }

  @AfterAll
  static void destroy() {
    SpringApplication.exit(context);
  }

  @Test
  void contextLoads() {}
}
