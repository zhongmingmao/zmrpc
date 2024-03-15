package io.zhongmingmao.zmrpc.demo.provider;

import io.zhongmingmao.zmrpc.core.api.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.RpcResponse;
import io.zhongmingmao.zmrpc.core.provider.ProviderBootstrap;
import io.zhongmingmao.zmrpc.core.provider.ProviderConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class ZmrpcDemoProviderApplication implements ApplicationRunner {

  ProviderBootstrap bootstrap;

  public static void main(String[] args) {
    SpringApplication.run(ZmrpcDemoProviderApplication.class, args);
  }

  @PostMapping("/")
  public RpcResponse invoke(@RequestBody RpcRequest request) {
    return bootstrap.invoke(request);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    RpcResponse response =
        bootstrap.invoke(
            RpcRequest.builder()
                .service("io.zhongmingmao.zmrpc.demo.api.UserService")
                .method("findById")
                .args(new Object[] {1024})
                .build());
    log.info("invoke result: {}", response);
  }
}
