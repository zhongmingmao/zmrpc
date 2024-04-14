package io.zhongmingmao.zmrpc.demo.consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.zhongmingmao.zmrpc.demo.api.user.User;
import io.zhongmingmao.zmrpc.demo.api.user.UserService;
import io.zhongmingmao.zmrpc.demo.consumer.controller.LoadBalancingController;
import io.zhongmingmao.zmrpc.demo.provider.DemoProviderApplication;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ConsumerTest {

  private static ConfigurableApplicationContext provider;
  private static ConfigurableApplicationContext consumer;
  private static UserService userService;

  @BeforeAll
  public static void beforeAll() {
    provider = SpringApplication.run(DemoProviderApplication.class, "--server.port=7001");
    consumer = SpringApplication.run(DemoConsumerApplication.class, "--server.port=7002");
    userService = consumer.getBean(LoadBalancingController.class).getUserService();
  }

  @AfterAll
  public static void afterAll() {
    SpringApplication.exit(consumer, () -> 1);
    SpringApplication.exit(provider, () -> 1);
  }

  @Test
  public void testConsumer() {}

  @Test
  public void testFindUser1() {
    log.info("testFindUser1, result: {}", userService.findUser());
  }

  @Test
  public void testFindUser2() {
    log.info("testFindUser2, result: {}", userService.findUser(1, "testFindUser2"));
  }

  @Test
  public void testFindUser3() {
    log.info(
        "testFindUser3, result: {}",
        userService.findUser(User.builder().id((long) (1 << 2)).name("testFindUser3").build()));
  }

  @Test
  public void testFindUserByShort() {
    log.info("testFindUserByShort, result: {}", userService.findUserByShort((short) (1 << 1)));
  }

  @Test
  public void testFindUserByFloat() {
    log.info("testFindUserByFloat, result: {}", userService.findUserByFloat(1 << 2));
  }

  @Test
  public void testFindUserByPrimitiveLong() {
    log.info(
        "testFindUserByPrimitiveLong, result: {}", userService.findUserByPrimitiveLong(1 << 3));
  }

  @Test
  public void testFindUserByLong1() {
    log.info("testFindUserByLong1, result: {}", userService.findUserByLong((long) -(1 << 4)));
  }

  public void testFindUserByLong2() {
    log.info("testFindUserByLong2, result: {}", userService.findUserByLong((long) (1 << 4)));
  }

  @Test
  public void testFindUserByName() {
    log.info("testFindUserByName, result: {}", userService.findUserByName("testFindUserByName"));
  }

  @Test
  public void testGetId() {
    log.info("testGetId, result: {}", userService.getId((long) (1 << 5)));
  }

  @Test
  public void testGetIds() {
    log.info("testGetIds, result: {}", userService.getIds(new long[] {1 << 1, 1 << 2, 1 << 3}));
  }

  @Test
  public void testGetUsers1() {
    Arrays.stream(
            userService.getUsers(
                new User[] {
                  User.builder().id((long) (1 << 1)).name("getUsers-1").build(),
                  User.builder().id((long) (1 << 2)).name("getUsers-2").build()
                }))
        .forEach(user -> log.info("testGetUsers1, result: {}", user));
  }

  @Test
  public void testGetUsers2() {
    Arrays.stream(
            userService.getUsers(
                Lists.newArrayList(
                    User.builder().id((long) (1 << 3)).name("getUsers-3").build(),
                    User.builder().id((long) (1 << 4)).name("getUsers-4").build())))
        .forEach(user -> log.info("testGetUsers2, result: {}", user));
  }

  @Test
  public void testGetUsers3() {
    Arrays.stream(
            userService.getUsers(
                Sets.newHashSet(
                    User.builder().id((long) (1 << 5)).name("getUsers-5").build(),
                    User.builder().id((long) (1 << 6)).name("getUsers-6").build())))
        .forEach(user -> log.info("testGetUsers3, result: {}", user));
  }

  @Test
  public void testGetUsers4() {
    Map<Integer, User> users = Maps.newHashMap();
    users.put(1 << 7, User.builder().id((long) (1 << 7)).name("getUsers-7").build());
    users.put(1 << 8, User.builder().id((long) (1 << 8)).name("getUsers-8").build());
    Arrays.stream(userService.getUsers(users))
        .forEach(user -> log.info("testGetUsers3, result: {}", user));
  }

  @Test
  public void testGetListUsers() {
    userService.getListUsers().forEach(user -> log.info("testGetListUsers, result: {}", user));
  }

  @Test
  public void testGetSetUsers() {
    userService.getSetUsers().forEach(user -> log.info("testGetSetUsers, result: {}", user));
  }

  @Test
  public void testGetMapUsers() {
    userService
        .getMapUsers()
        .forEach((id, user) -> log.info("testGetMapUsers, id: {}, user: {}", id, user));
  }

  @Test
  public void testGetName() {
    log.info("testGetName, result: {}", userService.getName((long) (1 << 10)));
  }

  @Test
  public void testReservedMethod() {
    log.info("testToString, result: {}", userService.toString());
    log.info("testHashCode, result: {}", userService.hashCode());
  }

  @Test
  public void testLoadBalancing() {
    int count = 1 << 3;
    log.info("testLoadBalancing start");
    for (int i = 0; i < count; i++) {
      log.info("==> testLoadBalancing , user: {}", userService.findUser());
    }
    log.info("testLoadBalancing end");
  }
}
