package ai.zhongmingmao.zmrpc.demo.provider;

import ai.zhongmingmao.zmrpc.core.annotation.ZmProvider;
import ai.zhongmingmao.zmrpc.demo.api.Order;
import ai.zhongmingmao.zmrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@ZmProvider
public class OrderServiceImpl implements OrderService {

  @Override
  public Order findById(Integer id) {
    return Order.builder().id(id).amount(0.99F).build();
  }
}
