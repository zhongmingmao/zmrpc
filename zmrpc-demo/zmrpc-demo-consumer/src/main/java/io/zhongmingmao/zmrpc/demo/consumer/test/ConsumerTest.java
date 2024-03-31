package io.zhongmingmao.zmrpc.demo.consumer.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.zhongmingmao.zmrpc.core.annotatation.ZmConsumer;
import io.zhongmingmao.zmrpc.demo.api.user.User;
import io.zhongmingmao.zmrpc.demo.api.user.UserService;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ConsumerTest {

  @ZmConsumer UserService userService;

  @Bean
  public ApplicationRunner test() {
    return args -> {
      testFindUser1();
      testFindUser2();
      testFindUser3();
      testFindUserByShort();
      testFindUserByFloat();
      testFindUserByPrimitiveLong();
      testFindUserByLong1();
      testFindUserByLong2();
      testFindUserByName();
      testGetId();
      testGetIds();
      testGetUsers1();
      testGetUsers2();
      testGetUsers3();
      testGetUsers4();
      testGetListUsers();
      testGetSetUsers();
      testGetMapUsers();
      testGetName();
      testReservedMethod();
    };
  }

  private void testFindUser1() {
    log.info("testFindUser1, result: {}", userService.findUser());
  }

  private void testFindUser2() {
    log.info("testFindUser2, result: {}", userService.findUser(1, "testFindUser2"));
  }

  private void testFindUser3() {
    log.info(
        "testFindUser3, result: {}",
        userService.findUser(User.builder().id((long) (1 << 2)).name("testFindUser3").build()));
  }

  private void testFindUserByShort() {
    log.info("testFindUserByShort, result: {}", userService.findUserByShort((short) (1 << 1)));
  }

  private void testFindUserByFloat() {
    log.info("testFindUserByFloat, result: {}", userService.findUserByFloat(1 << 2));
  }

  private void testFindUserByPrimitiveLong() {
    log.info(
        "testFindUserByPrimitiveLong, result: {}", userService.findUserByPrimitiveLong(1 << 3));
  }

  private void testFindUserByLong1() {
    log.info("testFindUserByLong1, result: {}", userService.findUserByLong((long) -(1 << 4)));
  }

  private void testFindUserByLong2() {
    log.info("testFindUserByLong2, result: {}", userService.findUserByLong((long) (1 << 4)));
  }

  private void testFindUserByName() {
    log.info("testFindUserByName, result: {}", userService.findUserByName("testFindUserByName"));
  }

  private void testGetId() {
    log.info("testGetId, result: {}", userService.getId((long) (1 << 5)));
  }

  private void testGetIds() {
    log.info("testGetIds, result: {}", userService.getIds(new long[] {1 << 1, 1 << 2, 1 << 3}));
  }

  private void testGetUsers1() {
    Arrays.stream(
            userService.getUsers(
                new User[] {
                  User.builder().id((long) (1 << 1)).name("getUsers-1").build(),
                  User.builder().id((long) (1 << 2)).name("getUsers-2").build()
                }))
        .forEach(user -> log.info("testGetUsers1, result: {}", user));
  }

  private void testGetUsers2() {
    Arrays.stream(
            userService.getUsers(
                Lists.newArrayList(
                    User.builder().id((long) (1 << 3)).name("getUsers-3").build(),
                    User.builder().id((long) (1 << 4)).name("getUsers-4").build())))
        .forEach(user -> log.info("testGetUsers2, result: {}", user));
  }

  private void testGetUsers3() {
    Arrays.stream(
            userService.getUsers(
                Sets.newHashSet(
                    User.builder().id((long) (1 << 5)).name("getUsers-5").build(),
                    User.builder().id((long) (1 << 6)).name("getUsers-6").build())))
        .forEach(user -> log.info("testGetUsers3, result: {}", user));
  }

  private void testGetUsers4() {
    Map<Integer, User> users = Maps.newHashMap();
    users.put(1 << 7, User.builder().id((long) (1 << 7)).name("getUsers-7").build());
    users.put(1 << 8, User.builder().id((long) (1 << 8)).name("getUsers-8").build());
    Arrays.stream(userService.getUsers(users))
        .forEach(user -> log.info("testGetUsers3, result: {}", user));
  }

  private void testGetListUsers() {
    userService.getListUsers().forEach(user -> log.info("testGetListUsers, result: {}", user));
  }

  private void testGetSetUsers() {
    userService.getSetUsers().forEach(user -> log.info("testGetSetUsers, result: {}", user));
  }

  private void testGetMapUsers() {
    userService
        .getMapUsers()
        .forEach((id, user) -> log.info("testGetMapUsers, id: {}, user: {}", id, user));
  }

  private void testGetName() {
    log.info("testGetName, result: {}", userService.getName((long) (1 << 10)));
  }

  private void testReservedMethod() {
    log.info("testToString, result: {}", userService.toString());
    log.info("testHashCode, result: {}", userService.hashCode());
  }
}
