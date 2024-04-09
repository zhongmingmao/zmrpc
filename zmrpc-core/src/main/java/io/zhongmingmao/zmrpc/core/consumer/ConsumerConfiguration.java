package io.zhongmingmao.zmrpc.core.consumer;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.LoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.RoundRobinLoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.router.Router;
import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import io.zhongmingmao.zmrpc.core.api.registry.StaticRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfiguration {

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  @Bean
  @Order(Integer.MIN_VALUE)
  public ApplicationRunner buildProxyConsumers(final ConsumerBootstrap bootstrap) {
    return args -> bootstrap.buildProxyConsumers();
  }

  @Bean
  public Router<?> router() {
    return Router.DEFAULT;
  }

  @Bean
  public LoadBalancer<?> loadBalancer() {
    return new RoundRobinLoadBalancer<>();
  }

  @Value("${static.registry.providers}")
  String staticProviders;

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Registry registry() {
    return new StaticRegistry(
        Lists.newArrayList(
            Splitter.on(",").omitEmptyStrings().trimResults().split(staticProviders)));
  }
}
