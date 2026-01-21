package com.qiu.rpc.fault.retry.impl;

import com.qiu.rpc.fault.retry.RetryStrategy;
import com.qiu.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @author qiu
 * @version 1.0
 * @className NoRetryStrategy
 * @packageName com.qiu.rpc.fault.retry.impl
 * @Description
 * @date 2026/1/20 18:24
 * @since 1.0
 */
public class NoRetryStrategy implements RetryStrategy {
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
