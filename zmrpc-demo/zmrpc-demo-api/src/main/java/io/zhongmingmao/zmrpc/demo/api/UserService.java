package io.zhongmingmao.zmrpc.demo.api;

public interface UserService {

  User findUser();

  User findUserByShort(final short id);

  User findUserByPrimitiveLong(final long id);

  User findUserByLong(final Long id);

  User findUserByName(final String name);

  long getId(final Long id);

  String getName(final Long id);
}
