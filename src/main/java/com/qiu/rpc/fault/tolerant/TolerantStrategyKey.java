package com.qiu.rpc.fault.tolerant;

import lombok.Getter;

/**
 * @author qiu
 * @version 1.0
 * @className TolerantStrategyKey
 * @packageName com.qiu.rpc.fault.tolerant
 * @Description
 * @date 2026/1/21 11:52
 * @since 1.0
 */
@Getter
public enum TolerantStrategyKey {

    FAIL_FAST("failFast"),
    FAIL_SAFE("failSafe"),
    ;

    private final String key;

    TolerantStrategyKey(String key) {
        this.key = key;
    }

    public static TolerantStrategyKey getByString(String key) {
        for (TolerantStrategyKey tolerantStrategyKey : TolerantStrategyKey.values()) {
            if (tolerantStrategyKey.key.equalsIgnoreCase(key)) {
                return tolerantStrategyKey;
            }
        }
        return null;
    }
}
