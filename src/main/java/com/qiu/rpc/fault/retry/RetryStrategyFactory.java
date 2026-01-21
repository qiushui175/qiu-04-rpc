package com.qiu.rpc.fault.retry;

import com.qiu.rpc.spi.SpiLoader;

/**
 * @author qiu
 * @version 1.0
 * @className RetryStrategyFactory
 * @packageName com.qiu.rpc.fault.retry
 * @Description
 * @date 2026/1/21 10:21
 * @since 1.0
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = getInstance("no");

    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
