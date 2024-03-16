package io.zhongmingmao.zmrpc.demo.provider;

import io.zhongmingmao.zmrpc.core.annotatation.ZmProvider;
import io.zhongmingmao.zmrpc.demo.api.User;
import io.zhongmingmao.zmrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  private static final String USER_NAME = "zhongmingmao";

  @Override
  public User findUser() {
    return findUserByPrimitiveLong(System.currentTimeMillis());
  }

  @Override
  public User findUserByShort(short id) {
    return findUserByPrimitiveLong(id);
  }

  @Override
  public User findUserByPrimitiveLong(long id) {
    return findUserByLong(id);
  }

  @Override
  public User findUserByLong(Long id) {
    if (id < 0) {
      throw new IllegalArgumentException("id must be greater than 0");
    }
    return User.builder().id(id).name(USER_NAME + "-" + id).build();
  }

  @Override
  public User findUserByName(String name) {
    long id = System.currentTimeMillis();
    return User.builder().id(id).name(USER_NAME + "-" + id).build();
  }

  @Override
  public long getId(Long id) {
    return id;
  }

  @Override
  public String getName(Long id) {
    return USER_NAME + "-" + id;
  }
}
