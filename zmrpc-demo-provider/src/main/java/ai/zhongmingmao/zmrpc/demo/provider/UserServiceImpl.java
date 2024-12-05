package ai.zhongmingmao.zmrpc.demo.provider;

import ai.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import ai.zhongmingmao.zmrpc.demo.api.User;
import ai.zhongmingmao.zmrpc.demo.api.UserService;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  private static final String NAME = "zhongmingmao";

  Environment environment;

  @Override
  public User findById(int id) {
    return User.builder()
        .id(id)
        .name(
            "%s-%s@%d"
                .formatted(
                    NAME, environment.getProperty("server.port"), System.currentTimeMillis()))
        .build();
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
    return user.getId();
  }

  @Override
  public long getId(float id) {
    return (long) id;
  }

  @Override
  public int[] getIds() {
    return new int[] {4, 5, 6};
  }

  @Override
  public int[] getIds(int[] ids) {
    return ids;
  }

  @Override
  public long[] getLongIds() {
    return new long[] {7, 19};
  }

  @Override
  public List<User> getUsers(List<User> users) {
    return users;
  }

  @Override
  public User[] getUsers(User[] users) {
    return users;
  }

  @Override
  public Map<String, User> getUsers(Map<String, User> users) {
    return users;
  }

  @Override
  public String getName() {
    return "%s-%d".formatted(NAME, System.currentTimeMillis());
  }

  @Override
  public String getName(int id) {
    return "zhongmingmao-%d".formatted(id);
  }
}
