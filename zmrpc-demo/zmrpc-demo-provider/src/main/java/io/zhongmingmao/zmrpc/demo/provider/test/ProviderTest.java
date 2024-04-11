package io.zhongmingmao.zmrpc.demo.provider.test;

import static io.zhongmingmao.zmrpc.core.util.JsonUtil.toJsonOrEmpty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequest;
import io.zhongmingmao.zmrpc.core.api.request.RpcRequestArg;
import io.zhongmingmao.zmrpc.core.provider.ProviderInvoker;
import io.zhongmingmao.zmrpc.core.util.MethodUtil;
import io.zhongmingmao.zmrpc.demo.api.user.User;
import io.zhongmingmao.zmrpc.demo.api.user.UserService;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Configuration
public class ProviderTest {

  private static final String SERVICE = UserService.class.getCanonicalName();

  ProviderInvoker invoker;

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
    String method = "findUser";
    RpcRequest request = RpcRequest.builder().service(SERVICE).method(method).build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUser2() {
    String method = "findUser";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(long.class.getCanonicalName())
                      .value(1 << 10)
                      .build(),
                  RpcRequestArg.builder()
                      .type(String.class.getCanonicalName())
                      .value("testFindUser2")
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUser3() {
    String method = "findUser";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(User.class.getCanonicalName())
                      .value(User.builder().id((long) (1 << 4)).name("testFindUser3").build())
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUserByShort() {
    String method = "findUserByShort";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(short.class.getCanonicalName()).value(1 << 5).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUserByFloat() {
    String method = "findUserByFloat";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(float.class.getCanonicalName()).value(1 << 6).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUserByPrimitiveLong() {
    String method = "findUserByPrimitiveLong";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(long.class.getCanonicalName()).value(1 << 7).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUserByLong1() {
    String method = "findUserByLong";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(Long.class.getCanonicalName())
                      .value(-(1 << 8))
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUserByLong2() {
    String method = "findUserByLong";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(Long.class.getCanonicalName()).value(1 << 8).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testFindUserByName() {
    String method = "findUserByName";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(String.class.getCanonicalName())
                      .value("testFindUserByName")
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetId() {
    String method = "getId";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(Long.class.getCanonicalName()).value(1 << 9).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetIds() {
    String method = "getIds";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(long[].class.getCanonicalName())
                      .value(new long[] {1, 1 << 1, 1 << 2})
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetUsers1() {
    String method = "getUsers";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(User[].class.getCanonicalName())
                      .value(
                          new User[] {
                            User.builder().id((long) (1 << 1)).name("getUsers-1").build(),
                            User.builder().id((long) (1 << 2)).name("getUsers-2").build()
                          })
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetUsers2() {
    String method = "getUsers";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(List.class.getCanonicalName())
                      .value(
                          Lists.newArrayList(
                              User.builder().id((long) (1 << 3)).name("getUsers-3").build(),
                              User.builder().id((long) (1 << 4)).name("getUsers-4").build()))
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetUsers3() {
    String method = "getUsers";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder()
                      .type(Set.class.getCanonicalName())
                      .value(
                          Sets.newHashSet(
                              User.builder().id((long) (1 << 5)).name("getUsers-5").build(),
                              User.builder().id((long) (1 << 6)).name("getUsers-6").build()))
                      .build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetUsers4() {
    String method = "getUsers";
    Map<Integer, User> users = Maps.newHashMap();
    users.put(1 << 7, User.builder().id((long) (1 << 7)).name("getUsers-7").build());
    users.put(1 << 8, User.builder().id((long) (1 << 8)).name("getUsers-8").build());
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(Map.class.getCanonicalName()).value(users).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetListUsers() {
    String method = "getListUsers";
    RpcRequest request = RpcRequest.builder().service(SERVICE).method(method).build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetSetUsers() {
    String method = "getSetUsers";
    RpcRequest request = RpcRequest.builder().service(SERVICE).method(method).build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetMapUsers() {
    String method = "getMapUsers";
    RpcRequest request = RpcRequest.builder().service(SERVICE).method(method).build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testGetName() {
    String method = "getName";
    RpcRequest request =
        RpcRequest.builder()
            .service(SERVICE)
            .method(method)
            .args(
                new RpcRequestArg[] {
                  RpcRequestArg.builder().type(Long.class.getCanonicalName()).value(1 << 10).build()
                })
            .build();
    log.info("==> test method: '{}', result: {}", method, toJsonOrEmpty(invoker.invoke(request)));
  }

  private void testReservedMethod() {
    for (Method method : Objects.class.getMethods()) {
      if (!MethodUtil.isReservedMethod(method.getName())) {
        continue;
      }
      RpcRequest request =
          RpcRequest.builder()
              .service(SERVICE)
              .method(method.getName())
              .args(
                  new RpcRequestArg[] {
                    RpcRequestArg.builder()
                        .type(Long.class.getCanonicalName())
                        .value(1 << 10)
                        .build()
                  })
              .build();
      log.error(
          "<== test reserved method: '{}', result: {}",
          method.getName(),
          toJsonOrEmpty(invoker.invoke(request)));
    }
  }
}
