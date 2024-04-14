package io.zhongmingmao.zmrpc.core.test;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddedZookeeper {

  TestingCluster cluster;

  @SneakyThrows
  public void start() {
    InstanceSpec spec = new InstanceSpec(null, 2181, -1, -1, true, -1, -1, -1);
    cluster = new TestingCluster(spec);
    cluster.start();
    cluster.getServers().forEach(server -> log.info("spec: {}", server.getInstanceSpec()));
    log.info("embedded zookeeper started");
  }

  @SneakyThrows
  public void stop() {
    cluster.stop();
    log.info("embedded zookeeper stopped");
  }
}
