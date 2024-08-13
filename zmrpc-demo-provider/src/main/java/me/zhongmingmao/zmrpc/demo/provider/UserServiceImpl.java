package me.zhongmingmao.zmrpc.demo.provider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.demo.api.User;
import me.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  @Value("${server.port}")
  String port;

  @Override
  public User ex(boolean flag) {
    if (flag) {
      throw new RuntimeException("just throw an exception");
    }
    return User.builder().id(-1).name("zhongmingmao-" + port).build();
  }

  @Override
  public User findById(int id) {
    return User.builder().id(id).name("zhongmingmao-" + port).build();
  }

  @Override
  public User findById(int id, String name) {
    return User.builder().id(id).name(name).build();
  }

  @Override
  public long getId(long id) {
    return id;
  }

  @Override
  public long getId(User user) {
    return user.getId().longValue();
  }

  @Override
  public long getId(float id) {
    return (long) id;
  }

  @Override
  public String getName() {
    return "zhongmingmao";
  }

  @Override
  public String getName(int id) {
    return "zhongmingmao-" + id;
  }

  @Override
  public int[] getIds() {
    return new int[] {1, 2, 3};
  }

  @Override
  public long[] getLongIds() {
    return new long[] {4, 5, 6};
  }

  @Override
  public int[] getIds(int[] ids) {
    return ids;
  }

  @Override
  public User[] getArray(User[] users) {
    return users;
  }

  @Override
  public List<User> getList(List<User> users) {
    return users;
  }

  @Override
  public Map<String, User> getMap(Map<String, User> users) {
    return users;
  }

  @SneakyThrows
  @Override
  public User timeout(int ms) {
    if (Objects.equals("8081", port)) {
      TimeUnit.MILLISECONDS.sleep(ms);
    }

    return User.builder().id(ms).name("zhongmingmao-" + port).build();
  }
}
