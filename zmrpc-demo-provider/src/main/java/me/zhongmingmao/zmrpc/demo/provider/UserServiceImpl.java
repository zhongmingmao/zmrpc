package me.zhongmingmao.zmrpc.demo.provider;

import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.demo.api.User;
import me.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  @Override
  public User findById(int id) {
    return User.builder().id(0).name("zhongmingmao").build();
  }

  @Override
  public int getId(int id) {
    return id;
  }

  @Override
  public String getName() {
    return "zhongmingmao";
  }
}
