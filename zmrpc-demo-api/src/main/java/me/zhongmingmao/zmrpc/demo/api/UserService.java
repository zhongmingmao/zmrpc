package me.zhongmingmao.zmrpc.demo.api;

public interface UserService {

  User findById(int id);

  User findById(int id, String name);

  long getId(long id);

  long getId(User user);

  long getId(float id);

  String getName();

  String getName(int id);

  int[] getIds();

  long[] getLongIds();

  int[] getIds(int[] ids);
}
