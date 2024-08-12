package me.zhongmingmao.zmrpc.demo.consumer;

import lombok.SneakyThrows;
import me.zhongmingmao.zmrpc.core.test.ZooKeeperEmbedded;
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
  static ZooKeeperEmbedded zookeeper;

  @SneakyThrows
  @BeforeAll
  static void init() {
    zookeeper = new ZooKeeperEmbedded();
    context =
        SpringApplication.run(
            ZmrpcDemoProviderApplication.class,
            "--server.port=8084",
            "--zmrpc.zkServer=" + zookeeper.connectString(),
            "--logging.level.me.zhongmingmao.zmrpc=debug");
  }

  @SneakyThrows
  @AfterAll
  static void destroy() {
    SpringApplication.exit(context);
    zookeeper.stop();
  }

  @Test
  void contextLoads() {}
}
