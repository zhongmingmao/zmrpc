package io.zhongmingmao.zmrpc.core.constant;

import com.google.common.collect.Sets;

import java.util.Set;

public interface RpcConstant {

  Set<String> PROHIBITED_METHODS =
      Sets.newHashSet(
          "getClass",
          "hashCode",
          "equals",
          "clone",
          "toString",
          "notify",
          "notifyAll",
          "wait",
          "finalize");
}
