package com.qiu.example.provider;

import com.qiu.example.common.service.UserService;
import com.qiu.example.provider.service.impl.UserServiceImpl;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.model.ServiceMetaInfo;
import com.qiu.rpc.registry.LocalRegistry;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import com.qiu.rpc.server.HttpServer;
import com.qiu.rpc.server.impl.VertxHttpServer;
import com.qiu.rpc.server.tcp.VertxTcpServer;

import java.util.concurrent.ExecutionException;

import static com.qiu.rpc.constant.RpcConstant.DEFAULT_SERVICE_GROUP;

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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 将信息发布到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        serviceMetaInfo.setServiceGroup(DEFAULT_SERVICE_GROUP);

        registry.register(serviceMetaInfo);

        // 服务提供
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(rpcConfig.getServerPort());

        // tcp
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }

}
