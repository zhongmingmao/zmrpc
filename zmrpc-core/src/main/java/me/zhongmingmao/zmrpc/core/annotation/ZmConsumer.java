package me.zhongmingmao.zmrpc.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // ZmProvider 作用在 TYPE 上
@Inherited
public @interface ZmConsumer {}
