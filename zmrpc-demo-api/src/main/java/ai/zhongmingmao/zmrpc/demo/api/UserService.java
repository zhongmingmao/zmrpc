package ai.zhongmingmao.zmrpc.demo.api;

public interface UserService {

  User findById(int id);

  User findById(int id, String name);

  long getId(long id);

  long getId(User user);

  long getId(float id);

  int[] getIds();

  int[] getIds(int[] ids);

  long[] getLongIds();

  String getName();

  String getName(int id);
}
