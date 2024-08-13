package me.zhongmingmao.zmrpc.demo.api;

import java.util.List;
import java.util.Map;

public interface UserService {

  User ex(boolean flag);

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

  User[] getArray(User[] users);

  List<User> getList(List<User> users);

  Map<String, User> getMap(Map<String, User> users);

  User timeout(int ms);
}
