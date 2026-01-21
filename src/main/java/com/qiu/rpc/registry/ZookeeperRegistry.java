package com.qiu.rpc.registry;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.0
 * @className ZookeeperRegistry
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/16 10:46
 * @since 1.0
 */

/**
 * Zookeeper 注册中心实现
 */
@Slf4j
public class ZookeeperRegistry implements Registry {

    private static final String ZK_ROOT_PATH = "/rpc_registry";

    private CuratorFramework client;

    /**
     * provider 本地注册缓存
     */
    private final Set<String> localRegistryCache = ConcurrentHashMap.newKeySet();

    /**
     * consumer 服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 已监听的 serviceKey
     */
    private final Set<String> watchedServiceKeys = ConcurrentHashMap.newKeySet();

    @Override
    public void init(RegistryConfig registryConfig, String role) {
        this.client = CuratorFrameworkFactory.builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        client.start();

        try {
            if (client.checkExists().forPath(ZK_ROOT_PATH) == null) {
                client.create().creatingParentsIfNeeded().forPath(ZK_ROOT_PATH);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init zk root path", e);
        }

        // ZK 使用临时节点，本身不需要 heartbeat
        if ("provider".equals(role)) {
            log.info("Zookeeper registry initialized for provider");
        }
    }

    /**
     * 注册服务（临时节点）
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        String servicePath = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceKey();
        String nodePath = servicePath + "/" + serviceMetaInfo.getServiceNodeKey();

        try {
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().creatingParentsIfNeeded().forPath(servicePath);
            }

            byte[] data = JSONUtil.toJsonStr(serviceMetaInfo)
                    .getBytes(StandardCharsets.UTF_8);

            if (client.checkExists().forPath(nodePath) == null) {
                client.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(nodePath, data);
            }

            localRegistryCache.add(nodePath);
            log.info("Service registered: {}", nodePath);
        } catch (Exception e) {
            throw new RuntimeException("Register service failed", e);
        }
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String nodePath = ZK_ROOT_PATH + "/"
                + serviceMetaInfo.getServiceKey() + "/"
                + serviceMetaInfo.getServiceNodeKey();

        try {
            if (client.checkExists().forPath(nodePath) != null) {
                client.delete().forPath(nodePath);
            }
            localRegistryCache.remove(nodePath);
        } catch (Exception e) {
            log.warn("Unregister failed: {}", nodePath, e);
        }
    }

    /**
     * 服务发现
     */
    @Override
    public List<ServiceMetaInfo> serverDiscovery(String serviceKey) {
        List<ServiceMetaInfo> cached = registryServiceCache.getServiceCache(serviceKey);
        if (CollUtil.isNotEmpty(cached)) {
            return cached;
        }

        String servicePath = ZK_ROOT_PATH + "/" + serviceKey;
        List<ServiceMetaInfo> result = new ArrayList<>();

        try {
            if (client.checkExists().forPath(servicePath) == null) {
                return result;
            }

            List<String> children = client.getChildren().forPath(servicePath);
            for (String child : children) {
                String fullPath = servicePath + "/" + child;
                byte[] data = client.getData().forPath(fullPath);
                ServiceMetaInfo metaInfo =
                        JSONUtil.toBean(new String(data, StandardCharsets.UTF_8),
                                ServiceMetaInfo.class);
                result.add(metaInfo);
            }

            registryServiceCache.writeServiceCache(serviceKey, result);
            watch(serviceKey);

        } catch (Exception e) {
            throw new RuntimeException("Service discovery failed", e);
        }

        return result;
    }

    /**
     * ZK 临时节点自带心跳，这里是空实现
     */
    @Override
    public void heartBeat() {
        // no-op
    }

    /**
     * 监听服务节点变化
     */
    @Override
    public void watch(String serviceKey) {
        if (!watchedServiceKeys.add(serviceKey)) {
            return;
        }

        String servicePath = ZK_ROOT_PATH + "/" + serviceKey;

        PathChildrenCache cache =
                new PathChildrenCache(client, servicePath, true);

        cache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                case CHILD_UPDATED:
                    log.info("Service changed: {}, clear cache", serviceKey);
                    registryServiceCache.removeServiceCache(serviceKey);
                    break;
                default:
                    break;
            }
        });

        try {
            cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            throw new RuntimeException("Watch service failed", e);
        }
    }

    @Override
    public void cleanLocalCache() {
        registryServiceCache.clearServiceCache();
    }

    @Override
    public void destroy() {
        for (String path : localRegistryCache) {
            try {
                if (client.checkExists().forPath(path) != null) {
                    client.delete().forPath(path);
                }
            } catch (Exception ignored) {
            }
        }
        localRegistryCache.clear();

        if (client != null) {
            client.close();
        }
    }

    @Override
    public void cleanLocalCache(String serviceKey) {
        registryServiceCache.removeServiceCache(serviceKey);
    }
}

