package me.zhongmingmao.zmrpc.core.test;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.TestingServer;

@Slf4j
public class ZooKeeperEmbedded {

  private static final int DEFAULT_PORT = 12181;

  private final TestingServer server;

  public ZooKeeperEmbedded() throws Exception {
    this(DEFAULT_PORT);
  }

  public ZooKeeperEmbedded(int port) throws Exception {
    log.debug("Starting embedded ZooKeeper server on port {} ...", port);
    this.server = new TestingServer(port);
  }

  public void stop() throws IOException {
    log.debug("Shutting down embedded ZooKeeper server at {} ...", server.getConnectString());
    server.close();
    log.debug("Shutdown of embedded ZooKeeper server at {} completed", server.getConnectString());
  }

  public String connectString() {
    return server.getConnectString();
  }

  public String hostname() {
    return connectString().substring(0, connectString().lastIndexOf(':'));
  }
}
