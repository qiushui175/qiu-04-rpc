package com.qiu.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.model.ServiceMetaInfo;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import com.qiu.rpc.serializer.Serializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qiu
 * @version 1.0
 * @className ServiceProxy
 * @packageName com.qiu.rpc.proxy
 * @Description
 * @date 2026/1/12 16:43
 * @since 1.0
 */
public class ServiceProxy implements InvocationHandler {

    Serializer serializer;

    public ServiceProxy(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .build();

        try {
            byte[] bytes = serializer.serialize(rpcRequest);
            byte[] res;

            // 获取到注册中心配置
            RegistryConfig registryConfig = RpcApplication.getRpcConfig().getRegistryConfig();
            Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());
            // 通过服务基本参数，去获取到地址和信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(rpcRequest.getServiceName());
            serviceMetaInfo.setServiceGroup(rpcRequest.getServiceGroup());
            serviceMetaInfo.setServiceVersion(rpcRequest.getServiceVersion());
            List<ServiceMetaInfo> serviceList = registry.serverDiscovery(serviceMetaInfo.getServiceKey());

            // TODO 对服务进行选择，现在简单的选择第一个
            ServiceMetaInfo chooseServiceInfo = serviceList.get(0);
            try (HttpResponse httpResponse =
                         HttpRequest.post(String.format("http://%s:%d",
                                         chooseServiceInfo.getServiceHost(), chooseServiceInfo.getServicePort()))
                                 .body(bytes)
                                 .execute()) {
                res = httpResponse.bodyBytes();
            }

            RpcResponse response = serializer.deserialize(res, RpcResponse.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
