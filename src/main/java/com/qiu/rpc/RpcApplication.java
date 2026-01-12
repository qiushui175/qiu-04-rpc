package com.qiu.rpc;

import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.constant.RpcConstant;
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

    public static void init(RpcConfig config) {
        rpcConfig = config;
        log.info("RpcApplication initialized with config: {}", rpcConfig);
    }

    public static void init() {
        RpcConfig newConfig;
        try {
            newConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            newConfig = new RpcConfig();
        }
        init(newConfig);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
