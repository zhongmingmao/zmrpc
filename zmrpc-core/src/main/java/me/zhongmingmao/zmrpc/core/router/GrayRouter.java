package me.zhongmingmao.zmrpc.core.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.zhongmingmao.zmrpc.core.api.Router;
import me.zhongmingmao.zmrpc.core.provider.InstanceMeta;
import org.springframework.util.CollectionUtils;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrayRouter implements Router<InstanceMeta> {

  int grayRatio;
  Random random = new Random();

  public GrayRouter(int grayRatio) {
    this.grayRatio = grayRatio;
  }

  @Override
  public List<InstanceMeta> route(List<InstanceMeta> providers) {
    if (CollectionUtils.isEmpty(providers) || providers.size() == 1) {
      return providers;
    }

    List<InstanceMeta> normalInstances = new ArrayList<>();
    List<InstanceMeta> grayInstances = new ArrayList<>();

    providers.forEach(
        instance -> {
          if (Objects.equals("true", instance.getParameters().get("gray"))) {
            grayInstances.add(instance);
          } else {
            normalInstances.add(instance);
          }
        });

    log.debug(
        "grayInstances/normalInstances, grayRatio ===> {}/{}, {}",
        grayInstances.size(),
        normalInstances.size(),
        grayRatio);

    if (CollectionUtils.isEmpty(normalInstances) || CollectionUtils.isEmpty(grayInstances)) {
      return providers;
    }

    if (grayRatio <= 0) {
      return normalInstances;
    }
    if (grayRatio >= 100) {
      return grayInstances;
    }

    // 需要与 LB 解耦，不能假设 LB 一定是均匀的
    // 在 A 情况下，返回 normalInstances；在 B 情况下，返回 grayInstances
    if (random.nextInt(100) < grayRatio) {
      log.debug("grayInstances ===> {}", grayInstances);
      return grayInstances;
    } else {
      log.debug("normalInstances ===> {}", normalInstances);
      return normalInstances;
    }
  }
}
