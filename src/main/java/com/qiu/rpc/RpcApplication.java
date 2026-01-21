package com.qiu.rpc;

import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.constant.RpcConstant;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import com.qiu.rpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qiu
 * @version 1.0
 * @className RpcApplication
 * @packageName com.qiu.rpc
 * @Description
 * @date 2026/1/12 17:52
 * @since 1.0
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig config, String role) {
        // 加载基础配置
        rpcConfig = config;
        log.info("RpcApplication initialized with config: {}", rpcConfig);
        config.setRole(role);

        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistryAndInit(registryConfig, rpcConfig.getRole());
        log.info("Registry initialized: {}", registryConfig);

        // 创建主动下线
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    public static void init(String role) {
        RpcConfig newConfig;
        try {
            newConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            newConfig = new RpcConfig();
        }
        init(newConfig, role == null ? newConfig.getRole() : role);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init(null);
                }
            }
        }
        return rpcConfig;
    }
}
