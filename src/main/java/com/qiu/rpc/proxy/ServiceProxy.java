package com.qiu.rpc.proxy;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.loadbalancer.LoadBalancer;
import com.qiu.rpc.loadbalancer.LoadBalancerFactory;
import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.model.ServiceMetaInfo;
import com.qiu.rpc.protocol.*;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import com.qiu.rpc.serializer.Serializer;
import com.qiu.rpc.server.tcp.VertxTcpFactory;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
//            byte[] bytes = serializer.serialize(rpcRequest);
//            byte[] res;

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
//            ServiceMetaInfo chooseServiceInfo = serviceList.get(0);
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(RpcApplication.getRpcConfig().getLoadBalancer());
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("serviceName", rpcRequest.getServiceName());
            requestParams.put("methodName", method.getName());
            ServiceMetaInfo chooseServiceInfo = loadBalancer.select(requestParams, serviceList);
            // 通过http方式进行调用
            //RpcResponse response = getResponseFromHttp(chooseServiceInfo, rpcRequest);
            RpcResponse response = getResponseFromTcp(chooseServiceInfo, rpcRequest);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private RpcResponse getResponseFromHttp(ServiceMetaInfo chooseServiceInfo, RpcRequest rpcRequest) throws Exception {
        byte[] bytes = serializer.serialize(rpcRequest);
        byte[] res;
        try (HttpResponse httpResponse =
                     HttpRequest.post(String.format("http://%s:%d",
                                     chooseServiceInfo.getServiceHost(), chooseServiceInfo.getServicePort()))
                             .body(bytes)
                             .execute()) {
            res = httpResponse.bodyBytes();
        }
        RpcResponse response = serializer.deserialize(res, RpcResponse.class);
        return response;
    }

    private RpcResponse getResponseFromTcp(ServiceMetaInfo chooseServiceInfo, RpcRequest rpcRequest) throws Exception {
        // 使用tcp方式进行调用
        NetClient netClient = VertxTcpFactory.getNetClientInstance();
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();
        netClient.connect(chooseServiceInfo.getServicePort(), chooseServiceInfo.getServiceHost(), connectResult -> {
            if (connectResult.succeeded()) {
                NetSocket resultSocket = connectResult.result();
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                header.setSerializationType(ProtocolMessageSerializerEnum.getByName(RpcApplication.getRpcConfig().getSerializer()).getCode());
                header.setMessageType((byte) ProtocolMessageTypeEnum.REQUEST.getType());
                header.setStatus((byte) ProtocolMessageStatusEnum.OK.getCode());
                header.setRequestId(IdUtil.getSnowflakeNextId());

                protocolMessage.setHeader(header);
                protocolMessage.setBody(rpcRequest);

                try {
                    // 编码
                    resultSocket.write(ProtocolMessageEncoder.encode(protocolMessage));
                } catch (Exception e) {
                    completableFuture.completeExceptionally(e);
                }

                resultSocket.handler(buffer -> {
                    try {
                        ProtocolMessage<RpcResponse> rpcResponse = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                        completableFuture.complete(rpcResponse.getBody());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            } else {
                completableFuture.completeExceptionally(connectResult.cause());
            }
        });

//        netClient.close();
        return completableFuture.get();
    }
}
