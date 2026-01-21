package com.qiu.rpc.fault.retry;

import com.qiu.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @author qiu
 * @version 1.0
 * @className RetryStrategy
 * @packageName com.qiu.rpc.fault.retry
 * @Description
 * @date 2026/1/20 18:23
 * @since 1.0
 */
public interface RetryStrategy {

    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;

}
