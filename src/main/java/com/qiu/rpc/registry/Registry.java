package com.qiu.rpc.registry;

import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author qiu
 * @version 1.0
 * @className Registry
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/14 10:48
 * @since 1.0
 */
public interface Registry {

    void init(RegistryConfig registryConfig,  String role);

    /**
     * 注册服务
     *
     * @param serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException;

    /**
     * 注销服务
     *
     * @param serviceMetaInfo
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    List<ServiceMetaInfo> serverDiscovery(String serviceKey);

    void destroy();

    void heartBeat();

    void watch(String serviceNodeKey);

    void cleanLocalCache();

    void cleanLocalCache(String serviceKey);
}
