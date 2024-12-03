package ai.zhongmingmao.zmrpc.demo.provider;

import ai.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import ai.zhongmingmao.zmrpc.demo.api.User;
import ai.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  private static final String NAME = "zhongmingmao";

  @Override
  public User findById(int id) {
    return User.builder().id(id).name("%s-%d".formatted(NAME, System.currentTimeMillis())).build();
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
  public String getName() {
    return "%s-%d".formatted(NAME, System.currentTimeMillis());
  }

  @Override
  public String getName(int id) {
    return "zhongmingmao-%d".formatted(id);
  }
}
