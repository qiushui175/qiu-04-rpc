package com.qiu.qiurpcspringbootstarter.annotation;

import com.qiu.qiurpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.qiu.qiurpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.qiu.qiurpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiu
 * @version 1.0
 * @className EnableRpc
 * @packageName com.qiu.qiurpcspringbootstarter.annotation
 * @Description
 * @date 2026/1/21 14:49
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {
    boolean needServer() default true;
}
