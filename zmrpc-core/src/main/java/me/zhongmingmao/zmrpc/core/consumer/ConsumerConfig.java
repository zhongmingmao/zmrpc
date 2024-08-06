package me.zhongmingmao.zmrpc.core.consumer;

import java.util.List;
import me.zhongmingmao.zmrpc.core.api.LoadBalancer;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.api.Router;
import me.zhongmingmao.zmrpc.core.cluster.RoundRobinLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

  @Value("${zmrpc.providers}")
  String servers;

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  // ApplicationContext 完全就绪后执行
  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner consumerBootstrapStart(@Autowired ConsumerBootstrap bootstrap) {
    return args -> {
      System.out.println("consumerBootstrapStart begin ...");
      bootstrap.start();
      System.out.println("consumerBootstrapStart end ...");
    };
  }

  @Bean
  public LoadBalancer loadBalancer() {
    return new RoundRobinLoadBalancer();
  }

  @Bean
  public Router router() {
    return Router.Default;
  }

  // 定义注册中心的启动关闭钩子
  @Bean(initMethod = "start", destroyMethod = "stop")
  public RegistryCenter registryCenter() {
    return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
  }
}
