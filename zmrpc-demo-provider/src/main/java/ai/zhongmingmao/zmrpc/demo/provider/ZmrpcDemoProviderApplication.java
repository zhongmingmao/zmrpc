package ai.zhongmingmao.zmrpc.demo.provider;

import ai.zhongmingmao.zmrpc.core.api.RpcRequest;
import ai.zhongmingmao.zmrpc.core.api.RpcResponse;
import ai.zhongmingmao.zmrpc.core.provider.ProviderBootstrap;
import ai.zhongmingmao.zmrpc.core.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class ZmrpcDemoProviderApplication {

  public static void main(String[] args) {
    SpringApplication.run(ZmrpcDemoProviderApplication.class, args);
  }

  @Autowired ProviderBootstrap providerBootstrap;

  @PostMapping("/")
  public RpcResponse invoke(@RequestBody RpcRequest request) {
    return providerBootstrap.invoke(request);
  }

  @Bean
  ApplicationRunner runner() {
    return args -> {
      RpcRequest request =
          RpcRequest.builder()
              .service("ai.zhongmingmao.zmrpc.demo.api.UserService")
              .method("findById")
              .args(new Object[] {1})
              .build();
      RpcResponse<?> response = providerBootstrap.invoke(request);
      System.out.println(response.getData());
    };
  }
}
