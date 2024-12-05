package ai.zhongmingmao.zmrpc.core.consumer;

import ai.zhongmingmao.zmrpc.core.api.LoadBalancer;
import ai.zhongmingmao.zmrpc.core.api.RegistryCenter;
import ai.zhongmingmao.zmrpc.core.api.Router;
import ai.zhongmingmao.zmrpc.core.cluster.RoundRobinLoadBalancer;
import ai.zhongmingmao.zmrpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner bootstrap(@Autowired ConsumerBootstrap bootstrap) {
    return args -> {
      System.out.println("consumer bootstrap...");
      bootstrap.start();
    };
  }

  @Bean
  public Router router() {
    return Router.DEFAULT;
  }

  @Bean
  public LoadBalancer loadBalancer() {
    return new RoundRobinLoadBalancer();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  public RegistryCenter registryCenter() {
    return new ZkRegistryCenter();
  }
}
