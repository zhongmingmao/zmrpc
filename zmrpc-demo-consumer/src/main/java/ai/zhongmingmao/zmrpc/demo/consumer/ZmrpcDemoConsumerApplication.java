package ai.zhongmingmao.zmrpc.demo.consumer;

import ai.zhongmingmao.zmrpc.core.annotation.ZmConsumer;
import ai.zhongmingmao.zmrpc.core.consumer.ConsumerConfig;
import ai.zhongmingmao.zmrpc.demo.api.Order;
import ai.zhongmingmao.zmrpc.demo.api.OrderService;
import ai.zhongmingmao.zmrpc.demo.api.User;
import ai.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class ZmrpcDemoConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ZmrpcDemoConsumerApplication.class, args);
  }

  @ZmConsumer UserService userService;
  @ZmConsumer OrderService orderService;

  @Bean
  ApplicationRunner consumerRunner() {
    return args -> {
      User user = userService.findById(1);
      System.out.println("rpc result, userService.findById(1) = " + user);

      int id = userService.getId(11);
      System.out.println("rpc result, userService.getId(11) = " + id);

      String name = userService.getName();
      System.out.println("rpc result, userService.getName() = " + name);

      System.out.println("-----");
      String string = userService.toString();
      System.out.println("rpc result, userService.toString() = " + string);
      int hashCode = userService.hashCode();
      System.out.println("rpc result, userService.hashCode() = " + hashCode);
      boolean equals = userService.equals(userService);
      System.out.println("rpc result, userService.equals(null) = " + equals);
      System.out.println("-----");

      Order order = orderService.findById(1);
      System.out.println("rpc result, orderService.findById(1) = " + order);

      //      Order order404 = orderService.findById(404);
      //      System.out.println("rpc result, orderService.findById(404) = " + order404);
    };
  }
}
