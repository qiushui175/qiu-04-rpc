package com.qiu.rpc.loadbalancer.impl;

import cn.hutool.core.collection.CollUtil;
import com.qiu.rpc.loadbalancer.LoadBalancer;
import com.qiu.rpc.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性 Hash 负载均衡（完全业务无关、无 key 关联）
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * Hash 环
     */
    private final TreeMap<Integer, ServiceMetaInfo> hashRing = new TreeMap<>();

    /**
     * 虚拟节点数量
     */
    private static final int VIRTUAL_NODES = 128;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestContext,
                                  List<ServiceMetaInfo> serviceMetaInfoList) {

        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            return null;
        }

        // 1. 构建 Hash 环
        hashRing.clear();
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            String nodeKey = serviceMetaInfo.getServiceNodeKey();
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                int hash = fnv1Hash(nodeKey + "#" + i);
                hashRing.put(hash, serviceMetaInfo);
            }
        }

        // 2. 对整个 requestContext 做 Hash（不解析、不约定 key）
        int requestHash = fnv1Hash(normalizeContext(requestContext));

        // 3. 顺时针选择节点
        Map.Entry<Integer, ServiceMetaInfo> entry = hashRing.ceilingEntry(requestHash);
        if (entry == null) {
            entry = hashRing.firstEntry();
        }

        return entry.getValue();
    }

    /**
     * 将 requestContext 规范化为稳定字符串
     */
    private String normalizeContext(Map<String, Object> requestContext) {
        if (requestContext == null || requestContext.isEmpty()) {
            return "EMPTY_CONTEXT";
        }

        // TreeMap 保证 key 顺序稳定
        TreeMap<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<String, Object> entry : requestContext.entrySet()) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted.toString();
    }

    /**
     * FNV-1 32bit Hash
     */
    private int fnv1Hash(String data) {
        final int FNV_PRIME = 0x01000193;
        int hash = 0x811c9dc5;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            hash ^= b;
            hash *= FNV_PRIME;
        }
        return hash & 0x7fffffff;
    }
}
