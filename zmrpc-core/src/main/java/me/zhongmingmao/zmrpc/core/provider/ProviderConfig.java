package me.zhongmingmao.zmrpc.core.provider;

import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.registry.zk.ZkRegistryCenter;
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

  @Bean
  public ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
    return new ProviderInvoker(providerBootstrap);
  }

  // 定义注册中心的启动关闭钩子
  //  @Bean(initMethod = "start", destroyMethod = "stop") - 启动后注册服务，反注册服务后关闭
  @Bean
  public RegistryCenter registryCenter() {
    return new ZkRegistryCenter();
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
