package io.zhongmingmao.zmrpc.demo.provider;

import static io.zhongmingmao.zmrpc.core.util.RpcUtil.buildRpcRequestArgs;

import com.google.common.collect.Lists;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;
import io.zhongmingmao.zmrpc.core.api.response.RpcResponse;
import io.zhongmingmao.zmrpc.core.provider.ProviderBootstrap;
import io.zhongmingmao.zmrpc.core.provider.ProviderConfig;
import io.zhongmingmao.zmrpc.core.util.JsonUtil;
import io.zhongmingmao.zmrpc.demo.api.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class DemoProviderApplication {

  ProviderBootstrap bootstrap;

  public static void main(String[] args) {
    SpringApplication.run(DemoProviderApplication.class, args);
  }

  @PostMapping("/")
  public RpcResponse<?> invoke(final @RequestBody RpcRequest request) {
    return bootstrap.invoke(request);
  }

  @Bean
  public ApplicationRunner test() {
    return args -> {
      long id = 1 << 10;
      Lists.newArrayList("findUserByLong", "getId", "getName")
          .forEach(
              method -> {
                RpcRequest request =
                    RpcRequest.builder()
                        .service(UserService.class.getCanonicalName())
                        .method(method)
                        .args(buildRpcRequestArgs(id))
                        .build();
                log.info(
                    "test '{}': {}", method, JsonUtil.toJsonOrEmpty(bootstrap.invoke(request)));
              });

      String methodName = "findUserByShort";
      RpcRequest request =
          RpcRequest.builder()
              .service(UserService.class.getCanonicalName())
              .method("findUserByShort")
              .args(
                  new RpcRequestArg[] {
                    RpcRequestArg.builder().type(short.class.getCanonicalName()).value(id).build()
                  })
              .build();
      log.info("test '{}': {}", methodName, JsonUtil.toJsonOrEmpty(bootstrap.invoke(request)));

      methodName = "findUserByPrimitiveLong";
      request =
          RpcRequest.builder()
              .service(UserService.class.getCanonicalName())
              .method("findUserByPrimitiveLong")
              .args(
                  new RpcRequestArg[] {
                    RpcRequestArg.builder().type(long.class.getCanonicalName()).value(id).build()
                  })
              .build();
      log.info("test '{}': {}", methodName, JsonUtil.toJsonOrEmpty(bootstrap.invoke(request)));

      methodName = "findUser";
      request =
          RpcRequest.builder()
              .service(UserService.class.getCanonicalName())
              .method("findUser")
              .build();
      log.info("test '{}': {}", methodName, JsonUtil.toJsonOrEmpty(bootstrap.invoke(request)));

      String method = "hashCode";
      request =
          RpcRequest.builder().service(UserService.class.getCanonicalName()).method(method).build();
      log.info(
          "test prohibited '{}': {}", method, JsonUtil.toJsonOrEmpty(bootstrap.invoke(request)));
    };
  }
}
