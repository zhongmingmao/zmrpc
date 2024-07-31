package me.zhongmingmao.zmrpc.demo.provider;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.zhongmingmao.zmrpc.core.api.RpcRequest;
import me.zhongmingmao.zmrpc.core.api.RpcResponse;
import me.zhongmingmao.zmrpc.core.provider.ProviderBootstrap;
import me.zhongmingmao.zmrpc.core.provider.ProviderConfig;
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

  @Autowired ProviderBootstrap providerBootstrap;

  @RequestMapping("/")
  public RpcResponse invoke(@RequestBody RpcRequest request) {
    return providerBootstrap.invoke(request);
  }

  // Spring 容器完全就绪后执行
  @Bean
  ApplicationRunner providerRun() {
    return args -> {
      RpcRequest request = new RpcRequest();
      request.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request.setMethod("findById");
      request.setArgs(new Object[] {100});

      RpcResponse response = invoke(request);
      System.out.println(response.getData());
    };
  }
}
