package me.zhongmingmao.zmrpc.demo.consumer;

import me.zhongmingmao.zmrpc.core.annotation.ZmConsumer;
import me.zhongmingmao.zmrpc.core.consumer.ConsumerConfig;
import me.zhongmingmao.zmrpc.demo.api.Order;
import me.zhongmingmao.zmrpc.demo.api.OrderService;
import me.zhongmingmao.zmrpc.demo.api.User;
import me.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsumerConfig.class)
public class ZmrpcDemoConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ZmrpcDemoConsumerApplication.class, args);
  }

  // 使用 @PostConstruct 阶段，通过 ApplicationContext 获取的 Bean 是尚未初始化好的
  // @ZmConsumer 作用于 Field，一开始为空，声明式
  // 可以考虑使用 InstantiationAwareBeanPostProcessor#postProcessProperties - 处理 Bean 的属性
  @ZmConsumer UserService userService; // 远程调用 Provider，需动态生成

  @ZmConsumer OrderService orderService;

  // ApplicationRunner - 此刻 ApplicationContext 完全就绪
  @Bean
  public ApplicationRunner consumerRun() {
    return args -> {
      User user = userService.findById(1);
      System.out.println("rpc result, user = " + user);

      Order order = orderService.findById(2);
      System.out.println("rpc result, order = " + order);

      //      Order order404 = orderService.findById(404);
      //      System.out.println("rpc result, order404 = " + order404);

      int userGetId = userService.getId(3);
      System.out.println("rpc result, userGetId = " + userGetId);

      String name = userService.getName();
      System.out.println("rpc result, name = " + name);

      //      orderService.toString();
      //      orderService.hashCode();
    };
  }
}
