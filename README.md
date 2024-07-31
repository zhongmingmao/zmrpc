<!-- TOC start (generated with https://github.com/derlin/bitdowntoc) -->

- [服务提供者](#)
   * [请求响应](#-1)
      + [RpcRequest](#rpcrequest)
      + [RpcResponse](#rpcresponse)
   * [服务契约](#-2)
   * [服务实现](#-3)
   * [服务注册](#-4)
      + [定义注解](#-5)
      + [使用注解](#-6)
      + [扫描服务](#-7)
      + [服务调用](#-8)
   * [测试用例](#-9)

<!-- TOC end -->

# 服务提供者

## 请求响应

> 在框架中定义
> 

### RpcRequest

```java
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcRequest {
  String service; // me.zhongmingmao.zmrpc.demo.api.UserService
  String method; // findById
  Object[] args; // 0
}
```

### RpcResponse

```java
@Data
public class RpcResponse<T> {
  boolean status;
  T data;
}
```

## 服务契约

```java
public interface UserService {
  User findById(int id);
}
```

## 服务实现

> 注册为 Spring Bean
> 

```java
@Component
public class UserServiceImpl implements UserService {

  @Override
  public User findById(int id) {
    return User.builder().id(0).name("zhongmingmao").build();
  }
}
```

> 通过 REST 暴露 - 采用 `HTTP` 协议 + `JSON` 序列化
> 

```java
@RestController
public class UserController {

  @RequestMapping("/")
  public RpcResponse invoke(@RequestBody RpcRequest request) {
    return new RpcResponse();
  }
}
```

## 服务注册

### 定义注解

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ZmProvider {}
```

### 使用注解

> 在服务提供者上使用注解 - 标记
> 

```java
@Component
@ZmProvider
public class UserServiceImpl implements UserService {

  @Override
  public User findById(int id) {
    return User.builder().id(0).name("zhongmingmao").build();
  }
}
```

### 扫描服务

> 通过 ApplicationContext 扫描带有 @ZmProvider 注解的 Bean
> 

```java
  @Setter ApplicationContext applicationContext;

  Map<String, Object> skeleton = new HashMap<>();

  @PostConstruct // init-method
  public void buildProviders() {
    Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ZmProvider.class);
    providers.values().forEach(this::genInterface);

    skeleton.forEach((service, provider) -> System.out.println(service + " -> " + provider));
  }

  private void genInterface(Object provider) {
    Class<?> service = provider.getClass().getInterfaces()[0];
    skeleton.put(service.getCanonicalName(), provider);
  }
```

### 服务调用

> 依据 RPC 上下文，通过反射调用实际的方法
> 

```java
  public RpcResponse invoke(RpcRequest request) {
    Object provider = skeleton.get(request.getService());

    try {
      Method[] methods = provider.getClass().getMethods();
      for (Method method : methods) {
        if (Objects.equals(method.getName(), request.getMethod())) {
          Object result = method.invoke(provider, request.getArgs());
          return new RpcResponse(true, result);
        }
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    return null;
  }
```

## 测试用例

```java
  // Spring 容器完全就绪后执行
  @Bean
  ApplicationRunner providerRun() {
    return args -> {
      RpcRequest request = new RpcRequest();
      request.setService("me.zhongmingmao.zmrpc.demo.api.UserService");
      request.setMethod("findById");
      request.setArgs(new Object[] {100});

      RpcResponse response = invoke(request);
      System.out.println(response.getData());
    };
  }
```