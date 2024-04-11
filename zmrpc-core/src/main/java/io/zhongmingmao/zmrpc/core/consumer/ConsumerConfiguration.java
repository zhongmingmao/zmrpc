package io.zhongmingmao.zmrpc.core.consumer;

import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.LoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.loadbalancer.RoundRobinLoadBalancer;
import io.zhongmingmao.zmrpc.core.api.lb.router.Router;
import io.zhongmingmao.zmrpc.core.api.registry.Registry;
import io.zhongmingmao.zmrpc.core.api.registry.ZookeeperRegistry;
import io.zhongmingmao.zmrpc.core.consumer.transport.http.HttpInvoker;
import io.zhongmingmao.zmrpc.core.consumer.transport.http.OkHttpInvoker;
import okhttp3.Request;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@Configuration
public class ConsumerConfiguration {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Registry registry(final Environment environment) {
    return new ZookeeperRegistry(environment);
  }

  @Bean
  public HttpInvoker<Request> httpInvoker() {
    return new OkHttpInvoker();
  }

  @Bean
  public ConsumerBootstrap consumerBootstrap(
      final Registry registry, final HttpInvoker<Request> httpInvoker) {
    return new ConsumerBootstrap(registry, httpInvoker);
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
}
