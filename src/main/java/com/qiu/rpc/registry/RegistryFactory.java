package com.qiu.rpc.registry;

import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.spi.SpiLoader;

/**
 * @author qiu
 * @version 1.0
 * @className RegistryFactory
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/14 11:17
 * @since 1.0
 */
public class RegistryFactory {

    static {
        SpiLoader.load(Registry.class);
    }

    public static Registry getRegistry(String protocol) {
        return SpiLoader.getInstance(Registry.class, protocol);
    }

    public static Registry getRegistryAndInit(RegistryConfig registryConfig) {
        Registry instance = SpiLoader.getInstance(Registry.class, registryConfig.getRegistry());
        instance.init(registryConfig);
        return instance;
    }
}
