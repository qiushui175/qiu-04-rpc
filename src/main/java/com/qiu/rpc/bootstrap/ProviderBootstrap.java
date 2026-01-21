package com.qiu.rpc.bootstrap;

import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.model.ServiceMetaInfo;
import com.qiu.rpc.model.ServiceRegisterInfo;
import com.qiu.rpc.registry.LocalRegistry;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import com.qiu.rpc.server.tcp.VertxTcpServer;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.qiu.rpc.constant.RpcConstant.DEFAULT_SERVICE_GROUP;

/**
 * @author qiu
 * @version 1.0
 * @className ProviderBootstrap
 * @packageName com.qiu.rpc.bootstrap
 * @Description
 * @date 2026/1/21 12:11
 * @since 1.0
 */
public class ProviderBootstrap {

    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // RPC 框架初始化
        RpcApplication.init("provider");

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());


            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            serviceMetaInfo.setServiceGroup(DEFAULT_SERVICE_GROUP);

            // 将信息发布到注册中心
            try {
                registry.register(serviceMetaInfo);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // http服务提供
        // HttpServer httpServer = new VertxHttpServer();
        // httpServer.doStart(rpcConfig.getServerPort());

        // tcp
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());

    }

}
