- [服务提供者](#服务提供者)
  - [请求响应](#请求响应)
    - [RpcRequest](#rpcrequest)
    - [RpcResponse](#rpcresponse)
  - [服务契约](#服务契约)
  - [服务实现](#服务实现)
  - [服务注册](#服务注册)
    - [定义注解](#定义注解)
    - [使用注解](#使用注解)
    - [扫描服务](#扫描服务)
    - [服务调用](#服务调用)
  - [测试用例](#测试用例)
- [服务消费者](#服务消费者)
  - [核心过程](#核心过程)
  - [编程界面](#编程界面)
  - [扩展逻辑](#扩展逻辑)
  - [匹配目标](#匹配目标)
  - [生成代理](#生成代理)
  - [模拟请求](#模拟请求)
  - [Provider 异常](#provider-异常)
    - [RpcResponse](#rpcresponse-1)
    - [Provider](#provider)
    - [Consumer](#consumer)
  - [类型转换](#类型转换)
    - [返回值为原生类型](#返回值为原生类型)
    - [返回值为 String](#返回值为-string)
    - [返回值为 long](#返回值为-long)
    - [参数为对象](#参数为对象)
    - [参数为 float](#参数为-float)
    - [返回值为 int 数组](#返回值为-int-数组)
    - [参数为 int 数组](#参数为-int-数组)
    - [返回值为 List](#返回值为-list)
    - [返回值为 Map](#返回值为-map)
  - [服务挡板](#服务挡板)
    - [Consumer](#consumer-1)
    - [Provider](#provider-1)
  - [方法重载](#方法重载)
    - [方法签名](#方法签名)
    - [Provider](#provider-2)
    - [Consumer](#consumer-2)

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

# 服务消费者

## 核心过程

1. 定义一个 ApplicationRunner，会在 ApplicationContext 完全就绪后被调用
2. 在 ApplicationContext 中扫描所有的 Bean
  1. 匹配 Bean 中的所有被 @ZmConsumer 修饰的字段 - Bean 是***被 Spring 增强过的子类***
3. 为被 @ZmConsumer 修饰的字段生成 JDK 动态代理，基于 Class 信息，发起模拟 HTTP 请求
  1. 对返回数据进行类型转换
  2. 处理 Provider 发生异常的场景
  3. 服务挡板 - Consumer 不能将类似 toString 和 hashCode 等方法调用到 Provider

## 编程界面

```java
  // 使用 @PostConstruct 阶段，通过 ApplicationContext 获取的 Bean 是尚未初始化好的
  // @ZmConsumer 作用于 Field，一开始为空，声明式
  // 可以考虑使用 InstantiationAwareBeanPostProcessor#postProcessProperties - 处理 Bean 的属性
  @ZmConsumer UserService userService; // 远程调用 Provider，需动态生成

  // ApplicationRunner - 此刻 ApplicationContext 完全就绪
  @Bean
  public ApplicationRunner consumerRun() {
    return args -> {
      User user = userService.findById(1);
      System.out.println(user);
    };
  }
```

## 扩展逻辑

<aside>
💡 ApplicationRunner - ApplicationContext 完全就绪后执行

</aside>

```java
@Configuration
public class ConsumerConfig {

  @Bean
  public ConsumerBootstrap consumerBootstrap() {
    return new ConsumerBootstrap();
  }

  // ApplicationContext 完全就绪后执行
  @Bean
  public ApplicationRunner consumerBootstrapStart(@Autowired ConsumerBootstrap bootstrap) {
    return args -> {
      bootstrap.start();
    };
  }
}
```

```java
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsumerBootstrap implements ApplicationContextAware {

  @Setter ApplicationContext applicationContext;

  Map<String, Object> stub = new HashMap<>();

  // 主要目的 - 为 @ZmConsumer 字段赋值 - 动态生成代理类，模拟 HTTP 请求
  // 通过 ApplicationRunner 调用，此时 ApplicationContext 已完全就绪
  public void start() {
    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {}
  }
}
```

## 匹配目标

> 通过 ApplicationContext 获取所有的 Bean，再去查找 Bean 中被 @ZmConsumer 修饰的字段
>

> 由于 bean.getClass() 是被 Spring 增强过的子类，需从父类中查找
>

```java
  // 主要目的 - 为 @ZmConsumer 字段赋值 - 动态生成代理类，模拟 HTTP 请求
  // 通过 ApplicationRunner 调用，此时 ApplicationContext 已完全就绪
  public void start() {
    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {
      Object bean = applicationContext.getBean(name);

      // bean.getClass() 是被 Spring 增强过的子类
      List<Field> fields = findAnnotatedField(bean.getClass());
    }
  }

  private List<Field> findAnnotatedField(Class<?> klass) {
    List<Field> annotatedFields = new ArrayList<>();

    while (klass != null) {
      Field[] fields = klass.getDeclaredFields();
      for (Field field : fields) {
        if (field.isAnnotationPresent(ZmConsumer.class)) {
          annotatedFields.add(field);
        }
      }

      // 由于 bean.getClass() 是被 Spring 增强过的子类
      // 因此，需要不停地向上寻找 @ZmConsumer 修饰的字段
      klass = klass.getSuperclass();
    }

    return annotatedFields;
  }
```

## 生成代理

> 为被 @ZmConsumer 修饰的字段生成 JDK 动态代理，基于 Class 信息，发起模拟 HTTP 请求
>
1. 将 RpcRequest 序列化成 JSON，向服务提供者发起 HTTP 请求
2. 服务提供者响应 JSON，服务消费者需要将 JSON 响应反序列化为 RpcResponse
3. 处理 RpcResponse，返回服务消费者方法预期的结果类型 - 反射 + 类型转换

```java
  public void start() {
    String[] names = applicationContext.getBeanDefinitionNames();
    for (String name : names) {
      Object bean = applicationContext.getBean(name);

      // bean.getClass() 是被 Spring 增强过的子类
      List<Field> fields = findAnnotatedField(bean.getClass());

      fields.forEach(
          field -> {
            System.out.println(" ===> " + field.getName());
            try {
              Class<?> service = field.getType();
              String serviceName = service.getCanonicalName();
              if (!stub.containsKey(serviceName)) {
                Object consumer = createConsumer(service); // 生成动态代理
                field.setAccessible(true);
                field.set(bean, consumer);
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  // service 为被 @ZmConsumer 修饰的字段的类型
  private Object createConsumer(Class<?> service) {
    return Proxy.newProxyInstance(
        service.getClassLoader(), new Class[] {service}, new ZmInvocationHandler(service));
  }
```

## 模拟请求

> JSON 反序列化后的结果类型为 JSONObject，需进行**类型转换**
>

```java
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  Class<?> service;

  public ZmInvocationHandler(Class<?> service) {
    this.service = service;
  }

  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    RpcRequest request = new RpcRequest();
    request.setService(service.getCanonicalName()); // service 为被 @ZmConsumer 修饰的字段的类型
    request.setMethod(method.getName());
    request.setArgs(args);

    RpcResponse rpcResponse = post(request);

    // 需进行数据类型转换
    if (rpcResponse.isStatus()) {
      JSONObject jsonResult = (JSONObject) rpcResponse.getData(); // 反序列化后为一个 JSONObject，并非预期的类型
      return jsonResult.toJavaObject(method.getReturnType()); // 数据类型转换
    }

    return null;
  }

  static final MediaType APPLICATION_JSON = MediaType.get("application/json");

  OkHttpClient client =
      new OkHttpClient.Builder()
          .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
          .connectTimeout(1, TimeUnit.SECONDS)
          .readTimeout(1, TimeUnit.SECONDS)
          .writeTimeout(1, TimeUnit.SECONDS)
          .build();

  private RpcResponse post(RpcRequest rpcRequest) {
    // 序列化请求
    String reqJson = JSON.toJSONString(rpcRequest);

    Request request =
        new Request.Builder()
            .url("http://127.0.0.1:8080/")
            .post(RequestBody.create(reqJson, APPLICATION_JSON))
            .build();

    try {
      System.out.println("===> reqJson = " + reqJson);
      String resJson = client.newCall(request).execute().body().string();
      System.out.println("===> resJson = " + resJson);
      // 反序列化响应
      RpcResponse rpcResponse = JSON.parseObject(resJson, RpcResponse.class);
      return rpcResponse;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
```

## Provider 异常

> Provider 可能会发生异常
>

```java
@Component
@ZmProvider
public class OrderServiceImpl implements OrderService {

  @Override
  public Order findById(Integer id) {
    if (id == 404) {
      throw new RuntimeException("404 Exception");
    }

    return new Order(id.longValue(), 15.6f);
  }
}
```

> Consumer 日志，不够健壮
>

```java
===> reqJson = {"args":[404],"method":"findById","service":"me.zhongmingmao.zmrpc.demo.api.OrderService"}
===> resJson = {"timestamp":"2024-08-03T13:07:10.297+00:00","status":500,"error":"Internal Server Error","path":"/"}
rpc result, order404 = null
```

### RpcResponse

> 增加异常上下文
>

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {
  boolean status;
  T data;
  Exception ex;
}
```

### Provider

> 处理异常时，返回异常信息
>

```java
  public RpcResponse invoke(RpcRequest request) {
    RpcResponse response = new RpcResponse();

    Object provider = skeleton.get(request.getService());
    try {
      Method method = findMethod(provider.getClass(), request.getMethod());
      Object result = method.invoke(provider, request.getArgs());

      response.setStatus(true);
      response.setData(result);
      return response;
      // 简化异常信息，无需将 Provider 完整堆栈返回给 Consumer
    } catch (InvocationTargetException e) {
      response.setEx(new RuntimeException(e.getTargetException().getMessage()));
    } catch (IllegalAccessException e) {
      response.setEx(new RuntimeException(e.getMessage()));
    }

    return response;
  }
```

### Consumer

> 依据 RpcResponse#status 来判断 Provider 是否发生了异常 - 堆栈传递
>

```java
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    RpcRequest request = new RpcRequest();
    request.setService(service.getCanonicalName()); // service 为被 @ZmConsumer 修饰的字段的类型
    request.setMethod(method.getName());
    request.setArgs(args);

    RpcResponse rpcResponse = post(request);

    // 需进行数据类型转换
    if (rpcResponse.isStatus()) {
      // 成功
      JSONObject jsonResult = (JSONObject) rpcResponse.getData(); // 反序列化后为一个 JSONObject，并非预期的类型
      return jsonResult.toJavaObject(method.getReturnType()); // 数据类型转换
    } else {
      // 异常
      Exception ex = rpcResponse.getEx();
      // ex.printStackTrace();
      throw new RuntimeException(ex); // 直接抛出，Provider 的异常能传递到 Consumer
    }
  }
```

## 类型转换

> 需要考虑的 Case 比较多
>

### 返回值为原生类型

```java
public interface UserService {
  int getId(int id);
}
```

> 转换出错
>

```java
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class com.alibaba.fastjson.JSONObject (java.lang.Integer is in module java.base of loader 'bootstrap'; com.alibaba.fastjson.JSONObject is in unnamed module of loader 'app')
```

> 如果是原生类型，直接返回
>

```java
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    RpcResponse rpcResponse = post(request);

    // 需进行数据类型转换
    if (rpcResponse.isStatus()) {
      // 成功
      Object data = rpcResponse.getData();
      if (method.getReturnType().isPrimitive()) { // 如果返回类型为原生类型，无需转换，直接返回值
        return data;
      }
      JSONObject jsonResult = (JSONObject) data; // 反序列化后为一个 JSONObject，并非预期的类型
      return jsonResult.toJavaObject(method.getReturnType()); // 数据类型转换
    } else {
			...
    }
  }	...
}
```

### 返回值为 String

```java
public interface UserService {
  String getName();
}
```

> 转换出错
>

```java
java.lang.ClassCastException: class java.lang.String cannot be cast to class com.alibaba.fastjson.JSONObject (java.lang.String is in module java.base of loader 'bootstrap'; com.alibaba.fastjson.JSONObject is in unnamed module of loader 'app')
```

> 基于 fast-json 的反序列化结果
>

```java
  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		...
    // 需进行数据类型转换
    if (rpcResponse.isStatus()) {
      // 成功
      Object data = rpcResponse.getData();
      if (!(data
          instanceof
          JSONObject)) { // 如果 fast-json 没有将 Response 反序列化为 JSONObject，直接返回，使用于原生类型，String 等
        return data;
      }
      JSONObject jsonResult = (JSONObject) data; // 反序列化后为一个 JSONObject，并非预期的类型
      return jsonResult.toJavaObject(method.getReturnType()); // 数据类型转换
    } else {
	    ...
    }
  }
```

### 返回值为 long

```java
public interface UserService {
  long getId(long id);
}
```

> 转换出错 - 经过网络传输，Consumer 进行反序列化时，采用的是 Integer，而非 Long
>

```java
===> reqJson = {"args":[3],"methodSign":"getId@1_long","service":"me.zhongmingmao.zmrpc.demo.api.UserService"}
===> resJson = {"status":true,"data":3,"ex":null}
2024-08-04T17:41:22.604+08:00  INFO 38585 --- [zmrpc-core] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2024-08-04T17:41:22.611+08:00 ERROR 38585 --- [zmrpc-core] [           main] o.s.boot.SpringApplication               : Application run failed

java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.Long (java.lang.Integer and java.lang.Long are in module java.base of loader 'bootstrap')
	at jdk.proxy2/jdk.proxy2.$Proxy59.getId(Unknown Source) ~[na:na]
	at me.zhongmingmao.zmrpc.demo.consumer.ZmrpcDemoConsumerApplication.lambda$consumerRun$0(ZmrpcDemoConsumerApplication.java:44) ~[classes/:na]
```

> 类型转换 - 兼容返回值为 Long 或者 long 的场景
>

```java
  /**
   * @param origin 被 fast json 反序列化后的对象
   * @param type method 预期的类型
   * @return
   */
  public static Object cast(Object origin, Class<?> type) {
    if (origin == null) {
      return null;
    }

    Class<?> klass = origin.getClass();
    if (type.isAssignableFrom(klass)) { // 类型是兼容的，type 是 origin 的父类
      return origin;
    }

    // Boolean / boolean
    if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
      return Boolean.valueOf(origin.toString());
    }

    // Byte / byte
    if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
      return Byte.valueOf(origin.toString());
    }

    // Character / char
    if (type.equals(Character.class) || type.equals(Character.TYPE)) {
      return origin.toString().charAt(0);
    }

    // Short / short
    if (type.equals(Short.class) || type.equals(Short.TYPE)) {
      return Short.valueOf(origin.toString());
    }

    // Integer / int
    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
      return Integer.valueOf(origin.toString());
    }

    // Long / long
    if (type.equals(Long.class) || type.equals(Long.TYPE)) {
      return Long.valueOf(origin.toString());
    }

    // Float / float
    if (type.equals(Float.class) || type.equals(Float.TYPE)) {
      return Float.valueOf(origin.toString());
    }

    // Double / double
    if (type.equals(Double.class) || type.equals(Double.TYPE)) {
      return Double.valueOf(origin.toString());
    }

    return null;
  }
```

### 参数为对象

```java
public interface UserService {
  long getId(User user);
}
```

> Provider 进行反序列化时会将请求对象反序列化为 `LinkedHashMap`，并非预期的 User 类型
>

```java
java.lang.IllegalArgumentException: argument type mismatch
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
```

> 在 Provider 反序列化请求时，还原为预期的类型
>

```java
  public RpcResponse invoke(RpcRequest request) {
			...
      // 处理成方法预期的类型
      Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
      ...
  }

  private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
    if (args == null || args.length == 0) {
      return args;
    }

    Object[] actuals = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
    }

    return actuals;
  }
```

```java
  public static Object cast(Object origin, Class<?> type) {
	  ...
    // LinkedHashMap
    if (origin instanceof HashMap map) {
      JSONObject jsonObject = new JSONObject(map);
      return jsonObject.toJavaObject(type);
    }
  }
```

### 参数为 float

```java
public interface UserService {
  long getId(float id);
}
```

> Provider 进行反序列化时会将 float 反序列化为 Double，不符合预期
>

> 处理方式同上
>

### 返回值为 int 数组

```java
public interface UserService {
  int[] getIds();
}
```

> Provider 返回为 null，被反序列化为 JSONArray
>

```java
===> reqJson = {"methodSign":"getIds@0","service":"me.zhongmingmao.zmrpc.demo.api.UserService"}
===> resJson = {"status":true,"data":[1,2,3],"ex":null}
rpc result, ids = null
```

> 兼容处理 JSONArray - 借助 Array 工具类
>

```java
  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		...

    // 需进行数据类型转换
    if (rpcResponse.isStatus()) {
      // 成功
      Object data = rpcResponse.getData();

      if (data instanceof JSONObject jsonResult) {
        // 反序列化后为一个 JSONObject，并非预期的类型，需进行类型转换
        return jsonResult.toJavaObject(method.getReturnType());
      } else if (data instanceof JSONArray jsonArray) {
        Object[] array = jsonArray.toArray();

        Class<?> componentType = method.getReturnType().getComponentType(); // 数组元素的类型
        Object resultArray = Array.newInstance(componentType, array.length); // 创建预期类型的数组

        for (int i = 0; i < array.length; i++) {
          Array.set(resultArray, i, array[i]); // 通过反射为数组元素赋值
        }

        return resultArray;
      } else {
        // fast-json 没有将 Response 反序列化为 JSONObject，适用于原生类型，String 等
        return TypeUtils.cast(data, method.getReturnType());
      }
    } else {
			...
    }
  }
```

### 参数为 int 数组

```java
public interface UserService {
  int[] getIds(int[] ids);
}
```

> Provider 返回 null
>

```java
===> reqJson = {"args":[[7,8,9]],"methodSign":"getIds@1_[I","service":"me.zhongmingmao.zmrpc.demo.api.UserService"}
===> resJson = {"status":true,"data":null,"ex":null}
rpc result, ids2 = null
```

> fast-json 在反序列化数组时，会被反序列化为 ArrayList
>

```java
  public static Object cast(Object origin, Class<?> type) {
    // LinkedHashMap
		...

    // Array
    if (type.isArray()) {
      // 会 fast-json 被反序列化为 ArrayList
      if (origin instanceof List list) {
        int length = list.size();
        Class<?> componentType = type.getComponentType();
        System.out.println("componentType => " + componentType.getCanonicalName());
        Object resultArray = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
          Array.set(resultArray, i, list.get(i));
        }
        return resultArray;
      }
    }

    // Boolean / boolean
		...

    return null;
  }
```

### 返回值为 List

```java
public interface UserService {
  List<User> getList(List<User> users);
}
```

> fast-json 反序列化响应时，同样会反序列化为 JSONArray
>

> List 是泛型类型，而 Java 是伪泛型
>

```java
    // 需进行数据类型转换
    Class<?> returnType = method.getReturnType();
    if (rpcResponse.isStatus()) {
      // 成功
      Object data = rpcResponse.getData();

      if (data instanceof JSONObject jsonResult) {
        // 反序列化后为一个 JSONObject，并非预期的类型，需进行类型转换
        return jsonResult.toJavaObject(returnType);
      } else if (data instanceof JSONArray jsonArray) {
					...
        } else if (List.class.isAssignableFrom(returnType)) { // 返回值为 List 类型
          List<Object> resultList = new ArrayList<>(array.length);
          Type genericReturnType = method.getGenericReturnType(); // 获取泛型类型
          // java.util.List<me.zhongmingmao.zmrpc.demo.api.User>
          System.out.println("genericReturnType ==> " + genericReturnType);
          if (genericReturnType instanceof ParameterizedType parameterizedType) { // 参数化类型
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            // class me.zhongmingmao.zmrpc.demo.api.User
            System.out.println("actualType ==> " + actualType);
            for (Object o : array) {
              resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
            }
          } else {
            resultList.add(Arrays.asList(array));
          }
          return resultList;
        } else {
          return null;
        }
```

> 请求日志
>

```java
===> reqJson = {"args":[[{"id":1,"name":"zhongmingmao"}]],"methodSign":"getList@1_java.util.List","service":"me.zhongmingmao.zmrpc.demo.api.UserService"}
===> resJson = {"status":true,"data":[{"id":1,"name":"zhongmingmao"}],"ex":null}
genericReturnType ==> java.util.List<me.zhongmingmao.zmrpc.demo.api.User>
actualType ==> class me.zhongmingmao.zmrpc.demo.api.User
rpc result, list = [User(id=1, name=zhongmingmao)]
```

### 返回值为 Map

```java
public interface UserService {
  Map<String, User> getMap(Map<String, User> users);
}
```

> 响应会被反序列化为 JSONObject
>

> Map 也是泛型类型，而 Java 是伪泛型
>

```java
      Object data = rpcResponse.getData();

      if (data instanceof JSONObject jsonResult) {
        if (Map.class.isAssignableFrom(returnType)) { // 返回值为 Map 类型
          Map<Object, Object> resultMap = new HashMap<>();
          Type genericReturnType = method.getGenericReturnType();
          // genericReturnType ==> java.util.Map<java.lang.String,
          // me.zhongmingmao.zmrpc.demo.api.User>
          System.out.println("genericReturnType ==> " + genericReturnType);
          if (genericReturnType instanceof ParameterizedType parameterizedType) {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            Class<?> keyType = (Class<?>) actualTypes[0];
            Class<?> valueType = (Class<?>) actualTypes[1];
            // class java.lang.String
            System.out.println("keyType ==> " + keyType);
            // class me.zhongmingmao.zmrpc.demo.api.User
            System.out.println("valueType ==> " + valueType);
            jsonResult.forEach(
                (k, v) -> {
                  Object key = TypeUtils.cast(k, keyType);
                  Object value = TypeUtils.cast(v, valueType);
                  resultMap.put(key, value);
                });
          }
          return resultMap;
        }

        // 反序列化后为一个 JSONObject，并非预期的类型，需进行类型转换
        return jsonResult.toJavaObject(returnType);
      } else if (data instanceof JSONArray jsonArray) {
      ...
```

> 请求日志
>

```java
===> reqJson = {"args":[{"zhongmingmao":{"id":1,"name":"zhongmingmao"}}],"methodSign":"getMap@1_java.util.Map","service":"me.zhongmingmao.zmrpc.demo.api.UserService"}
===> resJson = {"status":true,"data":{"zhongmingmao":{"id":1,"name":"zhongmingmao"}},"ex":null}
genericReturnType ==> java.util.Map<java.lang.String, me.zhongmingmao.zmrpc.demo.api.User>
keyType ==> class java.lang.String
valueType ==> class me.zhongmingmao.zmrpc.demo.api.User
rpc result, userMap = {zhongmingmao=User(id=1, name=zhongmingmao)}
```

## 服务挡板

> Object 的方法不应该通过 RPC 调用
>

```java
	// 更优雅	
  public static boolean checkLocalMethod(final Method method) {
    return method.getDeclaringClass().equals(Object.class);
  }
```

### Consumer

> toString / hashCode / equals 等方法不能发送到 Provider
>

```java
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZmInvocationHandler implements InvocationHandler {

  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 服务挡板
    String name = method.getName();
    if (name.equals("toString") || name.equals("hashCode") || name.equals("equals")) {
      // TBD，请求不发送到 Provider
      return null;
    }
    ...
	} 
	...
}
```

### Provider

> 同样不接受 toString / hashCode / equals 等方法
>

```java
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {

	...

  public RpcResponse invoke(RpcRequest request) {
    if (request.getMethod().equals("toString")
        || request.getMethod().equals("hashCode")
        || request.getMethod().equals("equals")) {
      return null;
    }
    ...
	}
```

## 方法重载

```java
public interface UserService {
  User findById(int id);
  User findById(int id, String name);
}
```

> Provider 调用会报错
>

```java
java.lang.IllegalArgumentException: wrong number of arguments
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]
```

> 解决方案 - 在 RpcRequest 中携带方法签名，便于在 Provider 端解析
>

```java
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcRequest {
  String service;
  String methodSign;
  Object[] args;
}
```

### 方法签名

> 计算方法签名 - 比对签名的性能优于反射（每次请求）
>

```java
  // 计算签名 - 方法名 + 方法参数（参数个数 + 参数类型）
  // 到 Provider 时也只需要比对预先计算好的签名即可，无需每次请求都进行反射操作
  public static String methodSign(Method method) {
    StringBuilder sb = new StringBuilder(method.getName());
    sb.append("@").append(method.getParameterCount());
    Arrays.stream(method.getParameterTypes()).forEach(c -> sb.append("_").append(c.getName()));
    return sb.toString();
  }
```

### Provider

```java
@Data
public class ProviderMeta {
  Method method;
  String methodSign;
  Object serviceImpl;
}
```

> MultiValueMap - `<Service, List<Method>>`
>

```java
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProviderBootstrap implements ApplicationContextAware {
	...
  MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
  ...
}
```

> 初始化时，计算 skeleton
>

```java
  private void genInterface(Object provider) {
    Class<?> service = provider.getClass().getInterfaces()[0];

    Method[] methods = service.getMethods();
    for (Method method : methods) {
      if (MethodUtils.checkLocalMethod(method)) {
        continue;
      }
      createProvider(service, provider, method);
    }
  }

  private void createProvider(Class<?> service, Object provider, Method method) {
    ProviderMeta meta = new ProviderMeta();
    meta.setMethod(method);
    meta.setServiceImpl(provider);
    meta.setMethodSign(MethodUtils.methodSign(method));
    System.out.println("createProvider, " + meta);
    skeleton.add(service.getCanonicalName(), meta);
  }
```

> 服务调用的匹配过程 - *Service → List<Method> → MethodSign*
>

```java
  public RpcResponse invoke(RpcRequest request) {
    RpcResponse response = new RpcResponse();

    // 查找 Service 的方法列表
    List<ProviderMeta> metas = skeleton.get(request.getService());
    try {
      // 通过方法签名匹配
      ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());

      Method method = meta.getMethod();
      Object result = method.invoke(meta.getServiceImpl(), request.getArgs());

      response.setStatus(true);
      response.setData(result);
      return response;
      // 简化异常信息，无需将 Provider 完整堆栈返回给 Consumer
    } catch (InvocationTargetException e) {
      response.setEx(new RuntimeException(e.getTargetException().getMessage()));
    } catch (IllegalAccessException e) {
      response.setEx(new RuntimeException(e.getMessage()));
    }

    return response;
  }

  private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
    return metas.stream()
        .filter(providerMeta -> Objects.equals(methodSign, providerMeta.getMethodSign()))
        .findFirst()
        .orElse(null);
  }
```

### Consumer

> 在服务调用时，计算方法签名
>

```java
  // 模拟 HTTP 请求
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		...

    RpcRequest request = new RpcRequest();
    request.setService(service.getCanonicalName()); // service 为被 @ZmConsumer 修饰的字段的类型
    request.setMethodSign(MethodUtils.methodSign(method)); // 计算方法签名
    request.setArgs(args);
		...
  }
```