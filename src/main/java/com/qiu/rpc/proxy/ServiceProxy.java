package com.qiu.rpc.proxy;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiu.rpc.RpcApplication;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.config.RpcConfig;
import com.qiu.rpc.fault.retry.RetryStrategy;
import com.qiu.rpc.fault.retry.RetryStrategyFactory;
import com.qiu.rpc.fault.tolerant.TolerantStrategy;
import com.qiu.rpc.fault.tolerant.TolerantStrategyFactory;
import com.qiu.rpc.loadbalancer.LoadBalancer;
import com.qiu.rpc.loadbalancer.LoadBalancerFactory;
import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.model.ServiceMetaInfo;
import com.qiu.rpc.protocol.*;
import com.qiu.rpc.registry.Registry;
import com.qiu.rpc.registry.RegistryFactory;
import com.qiu.rpc.serializer.Serializer;
import com.qiu.rpc.server.tcp.PendingRequestManager;
import com.qiu.rpc.server.tcp.TcpConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author qiu
 * @version 1.0
 * @className ServiceProxy
 * @packageName com.qiu.rpc.proxy
 * @Description
 * @date 2026/1/12 16:43
 * @since 1.0
 */
@Slf4j
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

        RpcResponse response = null;
        try {
            // 获取到注册中心配置
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistry());

            // 通过服务基本参数，去获取到地址和信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(rpcRequest.getServiceName());
            serviceMetaInfo.setServiceGroup(rpcRequest.getServiceGroup());
            serviceMetaInfo.setServiceVersion(rpcRequest.getServiceVersion());
            List<ServiceMetaInfo> serviceList = registry.serverDiscovery(serviceMetaInfo.getServiceKey());
//            log.info(String.valueOf(serviceList.size()));

            // 负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("serviceName", rpcRequest.getServiceName());
            requestParams.put("methodName", method.getName());
            ServiceMetaInfo chooseServiceInfo = loadBalancer.select(requestParams, serviceList);

            // 重试机制
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            response = retryStrategy.doRetry(() -> getResponseFromTcp(chooseServiceInfo, rpcRequest));

        } catch (Exception e) {
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(RpcApplication.getRpcConfig().getTolerantStrategy());
            response = tolerantStrategy.doTolerant(null, e);
        }

        return response.getData();
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
        return serializer.deserialize(res, RpcResponse.class);
    }

    private RpcResponse getResponseFromTcp(ServiceMetaInfo chooseServiceInfo, RpcRequest rpcRequest) throws Exception {
        if (chooseServiceInfo == null) {
            throw new RuntimeException("未找到可用服务提供者");
        }

        long requestId = IdUtil.getSnowflakeNextId();

        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializationType(ProtocolMessageSerializerEnum.getByName(
                RpcApplication.getRpcConfig().getSerializer()).getCode());
        header.setMessageType((byte) ProtocolMessageTypeEnum.REQUEST.getType());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getCode());
        header.setRequestId(requestId);

        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);

        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        PendingRequestManager.PENDING_REQUESTS.put(requestId, responseFuture);

        try {
//            log.info("port:" + chooseServiceInfo.getServicePort());
            // 获取连接包装器（包含 socket + 写入锁）
            TcpConnectionManager.ConnectionWrapper wrapper = TcpConnectionManager.getConnection(
                    chooseServiceInfo.getServiceHost(),
                    chooseServiceInfo.getServicePort()
            );

            io.vertx.core.net.NetSocket socket = wrapper.getSocketFuture()
                    .get(5, TimeUnit.SECONDS);

            // 先编码成 Buffer（这一步不需要锁，可以并行）
            io.vertx.core.buffer.Buffer encodedMsg = ProtocolMessageEncoder.encode(protocolMessage);

            // 使用安全写入，保证整个消息的字节原子地写入 socket
            wrapper.safeWrite(socket, encodedMsg);

            // 压测时适当加大超时时间（建议 5~10 秒）
            return responseFuture.get(5, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            log.error("RPC 请求超时, RequestId: {}", requestId);
            throw new RuntimeException("RPC 调用超时");
        } catch (Exception e) {
            log.error("RPC 发送请求异常", e);
            throw new RuntimeException("RPC 调用异常", e);
        } finally {
            CompletableFuture<RpcResponse> future = PendingRequestManager.PENDING_REQUESTS.remove(requestId);
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
        }
    }
}