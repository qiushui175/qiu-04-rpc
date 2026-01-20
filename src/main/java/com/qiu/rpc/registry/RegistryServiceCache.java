package com.qiu.rpc.registry;

import com.qiu.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.0
 * @className RegistryServiceCache
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/15 14:32
 * @since 1.0
 */
public class RegistryServiceCache {

    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    void writeServiceCache(String serviceName, List<ServiceMetaInfo> serviceMetaInfos) {
        serviceCache.put(serviceName, serviceMetaInfos);
    }

    List<ServiceMetaInfo> getServiceCache(String serviceName) {
        return serviceCache.get(serviceName);
    }

    void clearServiceCache() {
        serviceCache.clear();
    }

    void removeServiceCache(String serviceName) {
        serviceCache.remove(serviceName);
    }
}
