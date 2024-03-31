package io.zhongmingmao.zmrpc.demo.provider.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.demo.api.user.User;
import io.zhongmingmao.zmrpc.demo.api.user.UserService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  @Override
  public User findUser() {
    return User.builder().id(System.currentTimeMillis()).name("findUser()").build();
  }

  @Override
  public User findUser(long id, String name) {
    return User.builder().id(id).name(name + " - findUser(long id, String name)").build();
  }

  @Override
  public long findUser(User user) {
    return user.getId();
  }

  @Override
  public User findUserByShort(short id) {
    return User.builder().id((long) id).name("findUserByShort(short id)").build();
  }

  @Override
  public User findUserByFloat(float id) {
    return User.builder().id((long) id).name("findUserByFloat(float id)").build();
  }

  @Override
  public User findUserByPrimitiveLong(long id) {
    return User.builder().id(id).name("findUserByPrimitiveLong(long id)").build();
  }

  @Override
  public User findUserByLong(Long id) {
    if (Objects.nonNull(id) && id < 0) {
      throw new IllegalArgumentException("id must be greater than 0");
    }
    return User.builder().id(id).name("findUserByLong(Long id)").build();
  }

  @Override
  public User findUserByName(String name) {
    return User.builder()
        .id(System.currentTimeMillis())
        .name(name + " - findUserByName(String name)")
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
                    .name(user.getName() + " - getUsers(User[] users)")
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
                      .name(user.getName() + " - getUsers(List<User> users)")
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
                      .name(user.getName() + " - getUsers(Set<User> users)")
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
                      .name(entry.getValue() + " - getUsers(Map<Integer, User> users)")
                      .build())
          .toArray(User[]::new);
    }
    return new User[0];
  }

  @Override
  public List<User> getListUsers() {
    return Lists.newArrayList(
        User.builder().id(System.currentTimeMillis()).name("getListUsers()").build());
  }

  @Override
  public Set<User> getSetUsers() {
    return Sets.newHashSet(
        User.builder().id(System.currentTimeMillis()).name("getSetUsers()").build());
  }

  @Override
  public Map<Long, User> getMapUsers() {
    Map<Long, User> users = Maps.newHashMap();
    users.put(
        System.currentTimeMillis(),
        User.builder().id(System.currentTimeMillis()).name("getMapUsers()").build());
    return users;
  }

  @Override
  public String getName(Long id) {
    return id + " - getName(Long id)";
  }
}
