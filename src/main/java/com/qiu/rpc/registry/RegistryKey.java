package com.qiu.rpc.registry;

/**
 * @author qiu
 * @version 1.0
 * @className RegistryKey
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/14 11:15
 * @since 1.0
 */
public enum RegistryKey {

    ETCD("etcd"),
    ZOOKEEPER("zookeeper");

    private final String key;

    RegistryKey(String key) {
        this.key = key;
    }

    public static RegistryKey getByString(String key) {
        for (RegistryKey registryKey : RegistryKey.values()) {
            if (registryKey.key.equalsIgnoreCase(key)) {
                return registryKey;
            }
        }
        return null;
    }
}
