package com.qiu.qiurpcspringbootstarter.bootstrap;

import com.qiu.qiurpcspringbootstarter.annotation.EnableRpc;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.server.tcp.VertxTcpServer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author qiu
 * @version 1.0
 * @className RpcInitBootstrap
 * @packageName com.qiu.qiurpcspringbootstarter.bootstrap
 * @Description
 * @date 2026/1/21 15:13
 * @since 1.0
 */
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * spring初始化注册
     *
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");

        RpcApplication.init(null);

        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        } else {
            System.out.println("不启动server");
        }
    }
}
