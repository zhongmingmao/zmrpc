package ai.zhongmingmao.zmrpc.demo.api;

import java.util.List;
import java.util.Map;

public interface UserService {

  User findById(int id);

  User findById(int id, String name);

  long getId(long id);

  long getId(User user);

  long getId(float id);

  int[] getIds();

  int[] getIds(int[] ids);

  long[] getLongIds();

  List<User> getUsers(List<User> users);

  User[] getUsers(User[] users);

  Map<String, User> getUsers(Map<String, User> users);

  String getName();

  String getName(int id);
}
