package com.qiu.qiurpcspringbootstarter.bootstrap;

import com.qiu.qiurpcspringbootstarter.annotation.RpcService;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.model.ServiceMetaInfo;
import com.qiu.rpc.registry.LocalRegistry;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.ExecutionException;

import static com.qiu.rpc.constant.RpcConstant.DEFAULT_SERVICE_GROUP;

/**
 * @author qiu
 * @version 1.0
 * @className RpcProviderBootstrap
 * @packageName com.qiu.qiurpcspringbootstarter.bootstrap
 * @Description
 * @date 2026/1/21 15:14
 * @since 1.0
 */
public class RpcProviderBootstrap implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService == null) {
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();

            LocalRegistry.register(serviceName, beanClass);

            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getRegistry(rpcConfig.getRegistryConfig().getRegistry());
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

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
