package com.qiu.rpc.loadbalancer.impl;

import cn.hutool.core.collection.CollUtil;
import com.qiu.rpc.loadbalancer.LoadBalancer;
import com.qiu.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author qiu
 * @version 1.0
 * @className RandomLoadBalancer
 * @packageName com.qiu.rpc.loadbalancer.impl
 * @Description
 * @date 2026/1/20 16:14
 * @since 1.0
 */
public class RandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestContext, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (CollUtil.isEmpty(serviceMetaInfoList)){
            return null;
        }

        int size = serviceMetaInfoList.size();
        if (size == 1){
            return serviceMetaInfoList.get(0);
        }

        return serviceMetaInfoList.get(random.nextInt(size));
    }
}
