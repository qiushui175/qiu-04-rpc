package com.qiu.rpc.loadbalancer;

import com.qiu.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author qiu
 * @version 1.0
 * @className LoadBalancer
 * @packageName com.qiu.rpc.loadbalancer
 * @Description
 * @date 2026/1/20 16:02
 * @since 1.0
 */
public interface LoadBalancer {

    ServiceMetaInfo select(Map<String, Object> requestContext, List<ServiceMetaInfo> serviceMetaInfoList);

}
