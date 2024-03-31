package io.zhongmingmao.zmrpc.demo.provider;

import io.zhongmingmao.zmrpc.core.provider.ProviderConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ProviderConfiguration.class})
public class DemoProviderApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoProviderApplication.class, args);
  }
}
