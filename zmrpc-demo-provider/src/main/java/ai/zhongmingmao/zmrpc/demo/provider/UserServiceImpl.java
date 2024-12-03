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
  public int getId(int id) {
    return id;
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
