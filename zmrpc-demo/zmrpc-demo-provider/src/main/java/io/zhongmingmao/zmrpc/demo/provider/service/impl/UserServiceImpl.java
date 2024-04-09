package io.zhongmingmao.zmrpc.demo.provider.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.demo.api.user.User;
import io.zhongmingmao.zmrpc.demo.api.user.UserService;
import java.util.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  Environment environment;

  private String identity() {
    return environment.getProperty("server.port");
  }

  @Override
  public User findUser() {
    return User.builder().id(System.currentTimeMillis()).name("findUser()-" + identity()).build();
  }

  @Override
  public User findUser(long id, String name) {
    return User.builder()
        .id(id)
        .name(name + " - findUser(long id, String name)-" + identity())
        .build();
  }

  @Override
  public long findUser(User user) {
    return user.getId();
  }

  @Override
  public User findUserByShort(short id) {
    return User.builder().id((long) id).name("findUserByShort(short id)" + identity()).build();
  }

  @Override
  public User findUserByFloat(float id) {
    return User.builder().id((long) id).name("findUserByFloat(float id)" + identity()).build();
  }

  @Override
  public User findUserByPrimitiveLong(long id) {
    return User.builder().id(id).name("findUserByPrimitiveLong(long id)" + identity()).build();
  }

  @Override
  public User findUserByLong(Long id) {
    if (Objects.nonNull(id) && id < 0) {
      throw new IllegalArgumentException("id must be greater than 0");
    }
    return User.builder().id(id).name("findUserByLong(Long id)-" + identity()).build();
  }

  @Override
  public User findUserByName(String name) {
    return User.builder()
        .id(System.currentTimeMillis())
        .name(name + " - findUserByName(String name)-" + identity())
        .build();
  }

  @Override
  public long getId(Long id) {
    return id;
  }

  @Override
  public long[] getIds(long[] ids) {
    return ids;
  }

  @Override
  public User[] getUsers(User[] users) {
    return Arrays.stream(users)
        .map(
            user ->
                User.builder()
                    .id(user.getId())
                    .name(user.getName() + " - getUsers(User[] users)-" + identity())
                    .build())
        .toArray(User[]::new);
  }

  @Override
  public User[] getUsers(List<User> users) {
    if (Objects.nonNull(users)) {
      return users.stream()
          .map(
              user ->
                  User.builder()
                      .id(user.getId())
                      .name(user.getName() + " - getUsers(List<User> users)-" + identity())
                      .build())
          .toArray(User[]::new);
    }
    return new User[0];
  }

  @Override
  public User[] getUsers(Set<User> users) {
    if (Objects.nonNull(users)) {
      return users.stream()
          .map(
              user ->
                  User.builder()
                      .id(user.getId())
                      .name(user.getName() + " - getUsers(Set<User> users)-" + identity())
                      .build())
          .toArray(User[]::new);
    }
    return new User[0];
  }

  @Override
  public User[] getUsers(Map<Integer, User> users) {
    if (Objects.nonNull(users)) {
      return users.entrySet().stream()
          .map(
              entry ->
                  User.builder()
                      .id(Long.valueOf(entry.getKey()))
                      .name(
                          entry.getValue() + " - getUsers(Map<Integer, User> users)-" + identity())
                      .build())
          .toArray(User[]::new);
    }
    return new User[0];
  }

  @Override
  public List<User> getListUsers() {
    return Lists.newArrayList(
        User.builder().id(System.currentTimeMillis()).name("getListUsers()-" + identity()).build());
  }

  @Override
  public Set<User> getSetUsers() {
    return Sets.newHashSet(
        User.builder().id(System.currentTimeMillis()).name("getSetUsers()-" + identity()).build());
  }

  @Override
  public Map<Long, User> getMapUsers() {
    Map<Long, User> users = Maps.newHashMap();
    users.put(
        System.currentTimeMillis(),
        User.builder().id(System.currentTimeMillis()).name("getMapUsers()-" + identity()).build());
    return users;
  }

  @Override
  public String getName(Long id) {
    return id + " - getName(Long id)-" + identity();
  }
}
