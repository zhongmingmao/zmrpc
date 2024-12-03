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
      User u1 = userService.findById(1);
      System.out.println("rpc result, userService.findById(1) = " + u1);

      User u2 = userService.findById(2, "zhongmingmao");
      System.out.println("rpc result, userService.findById(2, \"zhongmingmao\") = " + u2);

      long id1 = userService.getId(11);
      System.out.println("rpc result, userService.getId(11) = " + id1);

      long id2 = userService.getId(User.builder().id(789).build());
      System.out.println("rpc result, userService.getId(User.builder().id(789).build()) = " + id2);

      long id3 = userService.getId(123.456f);
      System.out.println("rpc result, userService.getId(123.456f) = " + id3);

      int[] ids1 = userService.getIds();
      System.out.println("rpc result, userService.getIds() = " + java.util.Arrays.toString(ids1));

      int[] ids2 = userService.getIds(new int[] {1, 5});
      System.out.println(
          "rpc result, userService.getIds(new int[]{1, 5}) = " + java.util.Arrays.toString(ids2));

      long[] longIds = userService.getLongIds();
      System.out.println(
          "rpc result, userService.getLongIds() = " + java.util.Arrays.toString(longIds));

      String n1 = userService.getName();
      System.out.println("rpc result, userService.getName() = " + n1);

      String n2 = userService.getName(123);
      System.out.println("rpc result, userService.getName(123) = " + n2);

      String string = userService.toString();
      System.out.println("rpc result, userService.toString() = " + string);

      Order order = orderService.findById(1);
      System.out.println("rpc result, orderService.findById(1) = " + order);

      //      Order order404 = orderService.findById(404);
      //      System.out.println("rpc result, orderService.findById(404) = " + order404);
    };
  }
}
