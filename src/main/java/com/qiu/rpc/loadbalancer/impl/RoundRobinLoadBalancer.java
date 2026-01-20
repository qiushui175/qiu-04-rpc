package com.qiu.rpc.loadbalancer.impl;

import cn.hutool.core.collection.CollUtil;
import com.qiu.rpc.loadbalancer.LoadBalancer;
import com.qiu.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qiu
 * @version 1.0
 * @className RoundRobinLoadBalancer
 * @packageName com.qiu.rpc.loadbalancer
 * @Description
 * @date 2026/1/20 16:05
 * @since 1.0
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestContext, List<ServiceMetaInfo> serviceMetaInfoList) {

        if (CollUtil.isEmpty(serviceMetaInfoList)){
            return null;
        }

        int size = serviceMetaInfoList.size();
        if (size == 1){
            return serviceMetaInfoList.get(0);
        }

        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
