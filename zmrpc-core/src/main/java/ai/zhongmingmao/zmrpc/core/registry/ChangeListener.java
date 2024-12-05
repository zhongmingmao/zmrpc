package ai.zhongmingmao.zmrpc.core.registry;

@FunctionalInterface
public interface ChangeListener {
  void fire(Event event);
}
