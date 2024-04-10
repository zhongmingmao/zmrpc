package io.zhongmingmao.zmrpc.demo.consumer.controller;

import io.zhongmingmao.zmrpc.core.annotatation.ZmConsumer;
import io.zhongmingmao.zmrpc.demo.api.user.UserService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lb")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoadBalancingController {

  @ZmConsumer UserService userService;

  @GetMapping("/subscribe")
  public void subscribe() {
    int count = 1 << 3;
    log.info("testLoadBalancing start");
    for (int i = 0; i < count; i++) {
      log.info("==> testLoadBalancing , user: {}", userService.findUser());
    }
    log.info("testLoadBalancing end");
  }
}
