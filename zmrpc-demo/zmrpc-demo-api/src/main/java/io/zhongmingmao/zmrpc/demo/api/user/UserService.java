package io.zhongmingmao.zmrpc.demo.api.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

  User findUser();

  User findUser(final long id, final String name);

  long findUser(final User user);

  User findUserByShort(final short id);

  User findUserByFloat(final float id);

  User findUserByPrimitiveLong(final long id);

  User findUserByLong(final Long id);

  User findUserByName(final String name);

  long getId(final Long id);

  long[] getIds(final long[] ids);

  User[] getUsers(final User[] users);

  User[] getUsers(final List<User> users);

  User[] getUsers(final Set<User> users);

  User[] getUsers(final Map<Integer, User> users);

  List<User> getListUsers();

  Set<User> getSetUsers();

  Map<Long, User> getMapUsers();

  String getName(final Long id);
}
