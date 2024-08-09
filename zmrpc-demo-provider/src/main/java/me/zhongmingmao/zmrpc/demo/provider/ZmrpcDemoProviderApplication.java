package me.zhongmingmao.zmrpc.demo.provider;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.provider.ProviderConfig;
import me.zhongmingmao.zmrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  // Spring 容器完全就绪后执行
  @Bean
  ApplicationRunner providerRun() {
    return args -> {
      // test 1
      RpcRequest request1 = new RpcRequest();
      request1.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request1.setMethodSign("findById@1_int");
      request1.setArgs(new Object[] {100});

      RpcResponse response1 = invoke(request1);
      System.out.println(response1.getData());

      // test 2
      RpcRequest request2 = new RpcRequest();
      request2.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request2.setMethodSign("findById@2_int_java.lang.String");
      request2.setArgs(new Object[] {100, "AA"});

      RpcResponse response2 = invoke(request2);
      System.out.println(response2.getData());
    };
  }
}
