package me.zhongmingmao.zmrpc.demo.provider;

import me.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import me.zhongmingmao.zmrpc.demo.api.Order;
import me.zhongmingmao.zmrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class OrderServiceImpl implements OrderService {

  @Override
  public Order findById(Integer id) {
    return new Order(id.longValue(), 15.6f);
  }
}
