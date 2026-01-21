package com.qiu.qiurpcspringbootstarter.annotation;

import com.qiu.rpc.constant.RpcConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiu
 * @version 1.0
 * @className RpcService
 * @packageName com.qiu.qiurpcspringbootstarter.annotation
 * @Description
 * @date 2026/1/21 14:51
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    Class<?> interfaceClass() default void.class;

    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;
}
