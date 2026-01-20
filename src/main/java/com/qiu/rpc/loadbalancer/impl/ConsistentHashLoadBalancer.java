package com.qiu.rpc.loadbalancer.impl;

import cn.hutool.core.collection.CollUtil;
import com.qiu.rpc.loadbalancer.LoadBalancer;
import com.qiu.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author qiu
 * @version 1.0
 * @className ConsistentHashLoadBalancer
 * @packageName com.qiu.rpc.loadbalancer.impl
 * @Description
 * @date 2026/1/20 16:17
 * @since 1.0
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {


    private final TreeMap<Integer, ServiceMetaInfo> hashRing = new TreeMap<>();

    private static final int VIRTUAL_NODES = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestContext, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            return null;
        }

        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                int hash = getHash(serviceMetaInfo.getServiceNodeKey() + "-VN" + i);
                hashRing.put(hash, serviceMetaInfo);
            }
        }

        int requestHash = getHash(requestContext);

        Map.Entry<Integer, ServiceMetaInfo> entry = hashRing.ceilingEntry(requestHash);
        if (entry == null) {
            entry = hashRing.firstEntry();
        }
        return entry.getValue();
    }

    private int getHash(Object key) {
        String keyStr = key.toString();
        int hash = keyStr.hashCode();
        return hash & 0x7fffffff; // Ensure non-negative
    }
}
