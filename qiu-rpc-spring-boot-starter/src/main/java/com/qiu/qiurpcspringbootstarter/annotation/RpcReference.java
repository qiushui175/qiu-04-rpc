package com.qiu.qiurpcspringbootstarter.annotation;

import com.qiu.rpc.constant.RpcConstant;
import com.qiu.rpc.fault.retry.RetryStrategy;
import com.qiu.rpc.fault.retry.RetryStrategyKey;
import com.qiu.rpc.loadbalancer.LoadBalancerKeys;

/**
 * @author qiu
 * @version 1.0
 * @className RpcReference
 * @packageName com.qiu.qiurpcspringbootstarter.annotation
 * @Description
 * @date 2026/1/21 14:53
 * @since 1.0
 */
public @interface RpcReference {

    Class<?> interfaceClass() default void.class;

    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    String loadBalancer() default "roundRobin";

    String retryStrategy() default "fixedInterval";

    String tolerantStrategy() default "failSafe";

    boolean mock() default false;

}
