package com.qiu.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.serializer.Serializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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

            // TODO 这里的地址要动态发现
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:18080")
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
