package com.qiu.rpc.loadbalancer;

import com.qiu.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import com.qiu.rpc.spi.SpiLoader;

/**
 * @author qiu
 * @version 1.0
 * @className LoadBalancerFactory
 * @packageName com.qiu.rpc.loadbalancer
 * @Description
 * @date 2026/1/20 16:25
 * @since 1.0
 */
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    private static final LoadBalancer defaultLoadBalancer = new RoundRobinLoadBalancer();

    public static LoadBalancer getInstance(String loadBalancerKey) {
        return SpiLoader.getInstance(LoadBalancer.class, loadBalancerKey);
    }
}
