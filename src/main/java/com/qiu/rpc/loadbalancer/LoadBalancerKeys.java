package com.qiu.rpc.loadbalancer;

import com.qiu.rpc.registry.RegistryKey;
import lombok.Getter;

/**
 * @author qiu
 * @version 1.0
 * @className LoadBalancerKeys
 * @packageName com.qiu.rpc.loadbalancer
 * @Description
 * @date 2026/1/20 16:23
 * @since 1.0
 */
@Getter
public enum LoadBalancerKeys {

    ROUND_ROBIN("roundRobin"),
    RANDOM("random"),
    CONSISTENT_HASH("consistentHash");

    private final String key;

    LoadBalancerKeys(String key) {
        this.key = key;
    }

    public static LoadBalancerKeys getByString(String key) {
        for (LoadBalancerKeys loadBalancerKey : LoadBalancerKeys.values()) {
            if (loadBalancerKey.key.equalsIgnoreCase(key)) {
                return loadBalancerKey;
            }
        }
        return null;
    }

}
