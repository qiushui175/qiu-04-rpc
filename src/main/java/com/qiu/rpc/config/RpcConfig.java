package com.qiu.rpc.config;

import com.qiu.rpc.loadbalancer.LoadBalancerKeys;
import com.qiu.rpc.serializer.SerializerKeys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author qiu
 * @version 1.0
 * @className RpcConfig
 * @packageName com.qiu.rpc.config
 * @Description
 * @date 2026/1/12 17:33
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcConfig {

    private String name = "qiu-rpc";

    private String version = "1.0";

    private String role = "provider";

    private String serverHost = "localhost";

    private Integer serverPort = 18080;

    private boolean mock = false;

    private String serializer = SerializerKeys.KRYO.getKey();

    private RegistryConfig registryConfig = new RegistryConfig();

    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN.getKey();
}
