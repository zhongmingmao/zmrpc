package me.zhongmingmao.zmrpc.demo.provider;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.provider.ProviderConfig;
import me.zhongmingmao.zmrpc.core.provider.ProviderInvoker;
import me.zhongmingmao.zmrpc.demo.api.User;
import me.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@Import({ProviderConfig.class})
public class ZmrpcDemoProviderApplication {

  public static void main(String[] args) {
    SpringApplication.run(ZmrpcDemoProviderApplication.class, args);
  }

  @Autowired ProviderInvoker providerInvoker;

  @RequestMapping("/")
  public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
    return providerInvoker.invoke(request);
  }

  @Autowired UserService userService;

  @RequestMapping("/timeoutPorts")
  public RpcResponse<String> setTimeoutPorts(String timeoutPorts) {
    userService.setTimeoutPorts(timeoutPorts);
    RpcResponse<String> response = new RpcResponse<>();
    response.setStatus(true);
    response.setData("OK - " + timeoutPorts);
    return response;
  }

  // Spring 容器完全就绪后执行
  @Bean
  ApplicationRunner providerRun() {
    return args -> {
      // test 1
      System.out.println("Provider Case 1. >>===[基本测试：1个参数]===");
      RpcRequest request1 = new RpcRequest();
      request1.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request1.setMethodSign("findById@1_int");
      request1.setArgs(new Object[] {100});
      RpcResponse response1 = invoke(request1);
      System.out.println(response1.getData());
      System.out.println();

      // test 2
      System.out.println("Provider Case 2. >>===[基本测试：2个参数]===");
      RpcRequest request2 = new RpcRequest();
      request2.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request2.setMethodSign("findById@2_int_java.lang.String");
      request2.setArgs(new Object[] {100, "AA"});
      RpcResponse response2 = invoke(request2);
      System.out.println(response2.getData());
      System.out.println();

      // test 3
      System.out.println("Provider Case 3. >>===[复杂测试：参数类型为List<User>]===");
      RpcRequest request3 = new RpcRequest();
      request3.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request3.setMethodSign("getList@1_java.util.List");
      List<User> userList = new ArrayList<>();
      userList.add(new User(100, "ZM100"));
      userList.add(new User(101, "ZM101"));
      request3.setArgs(new Object[] {userList});
      RpcResponse response3 = invoke(request3);
      System.out.println(response3.getData());
      System.out.println();

      // test 4
      System.out.println("Provider Case 4. >>===[复杂测试：参数类型为Map<String, User>]===");
      RpcRequest request4 = new RpcRequest();
      request4.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request4.setMethodSign("getMap@1_java.util.Map");
      Map<String, User> userMap = new HashMap<>();
      userMap.put("P1", new User(100, "ZM100"));
      userMap.put("P2", new User(101, "ZM101"));
      request4.setArgs(new Object[] {userMap});
      RpcResponse response4 = invoke(request4);
      System.out.println(response4.getData());
      System.out.println();
    };
  }
}
