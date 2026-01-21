package com.qiu.rpc.fault.tolerant;

import com.qiu.rpc.spi.SpiLoader;

/**
 * @author qiu
 * @version 1.0
 * @className TolerantStrategyFactory
 * @packageName com.qiu.rpc.fault.tolerant
 * @Description
 * @date 2026/1/21 11:54
 * @since 1.0
 */
public class TolerantStrategyFactory {

    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    public static TolerantStrategy DEFAULT = SpiLoader.getInstance(TolerantStrategy.class, "failFast");

    public static TolerantStrategy getInstance(String key) {
        TolerantStrategy tolerantStrategy = SpiLoader.getInstance(TolerantStrategy.class, key);
        if (tolerantStrategy == null) {
            return DEFAULT;
        }
        return tolerantStrategy;
    }
}
