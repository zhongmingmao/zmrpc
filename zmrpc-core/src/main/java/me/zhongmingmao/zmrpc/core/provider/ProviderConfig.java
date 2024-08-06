package me.zhongmingmao.zmrpc.core.provider;

import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.registry.ZkRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {

  @Bean
  ProviderBootstrap providerBootstrap() {
    return new ProviderBootstrap();
  }

  // 定义注册中心的启动关闭钩子
  @Bean(initMethod = "start", destroyMethod = "stop")
  public RegistryCenter registryCenter() {
    return new ZkRegistry();
  }
}
