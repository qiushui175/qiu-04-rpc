package com.qiu.example.provider;

import com.qiu.example.common.service.UserService;
import com.qiu.example.provider.service.impl.UserServiceImpl;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.registry.LocalRegistry;
import com.qiu.rpc.server.HttpServer;
import com.qiu.rpc.server.impl.VertxHttpServer;

import java.util.List;

/**
 * @author qiu
 * @version 1.0
 * @className ProviderExample
 * @packageName com.qiu.example.provider
 * @Description
 * @date 2026/1/9 15:41
 * @since 1.0
 */
public class ProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 服务提供
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(18080);
    }

}
