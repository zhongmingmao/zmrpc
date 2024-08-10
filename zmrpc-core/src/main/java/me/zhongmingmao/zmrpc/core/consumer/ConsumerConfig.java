package me.zhongmingmao.zmrpc.core.consumer;

import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.api.Filter;
import me.zhongmingmao.zmrpc.core.api.LoadBalancer;
import me.zhongmingmao.zmrpc.core.api.RegistryCenter;
import me.zhongmingmao.zmrpc.core.api.Router;
import me.zhongmingmao.zmrpc.core.cluster.RoundRobinLoadBalancer;
import me.zhongmingmao.zmrpc.core.filter.CacheFilter;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import me.zhongmingmao.zmrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
public class ConsumerConfig {

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  // ApplicationContext 完全就绪后执行
  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner consumerBootstrapStart(@Autowired ConsumerBootstrap bootstrap) {
    return args -> {
      log.info("consumerBootstrapStart begin ...");
      bootstrap.start();
      log.info("consumerBootstrapStart end ...");
    };
  }

  @Bean
  public Filter filter() {
    return new CacheFilter();
  }

  @Bean
  public LoadBalancer<InstanceMeta> loadBalancer() {
    return new RoundRobinLoadBalancer<>();
  }

  @Bean
  public Router<InstanceMeta> router() {
    return Router.DEFAULT;
  }

  // 定义注册中心的启动关闭钩子
  @Bean(initMethod = "start", destroyMethod = "stop")
  public RegistryCenter registryCenter() {
    return new ZkRegistryCenter();
  }
}
