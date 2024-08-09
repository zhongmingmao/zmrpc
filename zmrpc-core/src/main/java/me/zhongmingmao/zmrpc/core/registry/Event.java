package me.zhongmingmao.zmrpc.core.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {
  List<InstanceMeta> data;
}
