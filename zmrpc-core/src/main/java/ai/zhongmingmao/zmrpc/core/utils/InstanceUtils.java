package ai.zhongmingmao.zmrpc.core.utils;

import java.net.UnknownHostException;

public final class InstanceUtils {

  public static String buildInstance(String host, int port) throws UnknownHostException {
    return String.join("_", host, String.valueOf(port));
  }

  public static String buildProvider(String provider) {
    String[] split = provider.split("_");
    if (split.length != 2) {
      return provider;
    }
    return "http://%s:%s".formatted(split[0], split[1]);
  }
}
