package me.zhongmingmao.zmrpc.demo.provider;

import lombok.SneakyThrows;
import me.zhongmingmao.zmrpc.core.test.ZooKeeperEmbedded;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZmrpcDemoProviderApplicationTests {

  static ZooKeeperEmbedded zookeeper;

  @SneakyThrows
  @BeforeAll
  static void init() {
    zookeeper = new ZooKeeperEmbedded();
  }

  @SneakyThrows
  @AfterAll
  static void destroy() {
    zookeeper.stop();
  }

  @Test
  void contextLoads() {}
}
