package me.zhongmingmao.zmrpc.core.provider;

import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.registry.ZkRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {

  @Bean
  public ProviderBootstrap providerBootstrap() {
    return new ProviderBootstrap();
  }

  // 定义注册中心的启动关闭钩子
  @Bean(initMethod = "start", destroyMethod = "stop")
  public RegistryCenter registryCenter() {
    return new ZkRegistry();
  }

  @Bean
  public ApplicationRunner providerBootstrapStart(@Autowired ProviderBootstrap providerBootstrap) {
    return args -> {
      System.out.println("providerBootstrapStart start");
      providerBootstrap.start(); // 延迟注册 - 只有在 Spring 上下文完全就绪后，应用才具备接收请求的能力，此刻才执行注册动作
      System.out.println("providerBootstrapStart end");
    };
  }
}
