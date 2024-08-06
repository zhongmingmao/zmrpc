package me.zhongmingmao.zmrpc.core.api;

import java.util.List;

public interface Router<T> {

  /** 从一个大集合中筛选出一个小集合，适用于分区亲和性等场景 */
  List<T> route(List<T> providers);

  Router Default = p -> p;
}
