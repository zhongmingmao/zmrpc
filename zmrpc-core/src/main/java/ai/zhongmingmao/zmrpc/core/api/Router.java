package ai.zhongmingmao.zmrpc.core.api;

import java.util.List;

public interface Router<T> {

  List<T> choose(List<T> providers);

  Router DEFAULT = providers -> providers;
}
