package com.qiu.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.0
 * @className LocalRegistry
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/9 16:03
 * @since 1.0
 */
public class LocalRegistry {

    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName
     * @param serviceClass
     */
    public static void register(String serviceName, Class<?> serviceClass) {
        map.put(serviceName, serviceClass);
    }

    /**
     * 获取服务
     *
     * @param serviceName
     * @return
     */
    public static Class<?> getService(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * 移除服务
     *
     * @param serviceName
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
