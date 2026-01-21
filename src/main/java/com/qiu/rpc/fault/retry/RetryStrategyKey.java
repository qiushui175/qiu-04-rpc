package com.qiu.rpc.fault.retry;

import lombok.Getter;

/**
 * @author qiu
 * @version 1.0
 * @className RetryStrategyKey
 * @packageName com.qiu.rpc.fault.retry
 * @Description
 * @date 2026/1/21 10:19
 * @since 1.0
 */
@Getter
public enum RetryStrategyKey {

    NO("no"),
    FIXED_INTERVAL("fixedInterval");

    private final String key;

    RetryStrategyKey(String key) {
        this.key = key;
    }

    public static RetryStrategyKey getByString(String key) {
        for (RetryStrategyKey retryStrategyKey : RetryStrategyKey.values()) {
            if (retryStrategyKey.key.equalsIgnoreCase(key)) {
                return retryStrategyKey;
            }
        }
        return null;
    }

}
