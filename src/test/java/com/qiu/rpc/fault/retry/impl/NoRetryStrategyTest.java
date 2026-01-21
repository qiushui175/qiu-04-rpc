package com.qiu.rpc.fault.retry.impl;

import com.qiu.rpc.model.RpcResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author qiu
 * @version 1.0
 * @className NoRetryStrategyTest
 * @packageName com.qiu.rpc.fault.retry.impl
 * @Description
 * @date 2026/1/21 10:12
 * @since 1.0
 */
class NoRetryStrategyTest {
    @Test
    public void doRetry() {
        NoRetryStrategy noRetryStrategy = new NoRetryStrategy();
        FixedIntervalRetryStrategy fixedIntervalRetryStrategy = new FixedIntervalRetryStrategy();
        try {
            RpcResponse result = fixedIntervalRetryStrategy.doRetry(() -> {
                System.out.println("触发调用");
                throw new RuntimeException("调用失败");
            });
            System.out.println(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }
}