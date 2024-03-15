package io.zhongmingmao.zmrpc.demo.provider;

import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.demo.api.User;
import io.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  @Override
  public User findById(int id) {
    return User.builder().id(id).name("zhongmingmao-" + System.currentTimeMillis()).build();
  }
}
