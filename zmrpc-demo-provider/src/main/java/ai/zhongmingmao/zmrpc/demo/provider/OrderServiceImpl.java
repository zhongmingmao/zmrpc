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
    if (id == 404) {
      throw new RuntimeException("404 exception");
    }
    return Order.builder().id(id).amount(0.99F).build();
  }
}
