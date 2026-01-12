package com.qiu.rpc.server.handler;

import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.registry.LocalRegistry;
import com.qiu.rpc.serializer.Serializer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

import java.lang.reflect.Method;

/**
 * @author qiu
 * @version 1.0
 * @className HttpServerHandler
 * @packageName com.qiu.rpc.server.handler
 * @Description
 * @date 2026/1/12 16:10
 * @since 1.0
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    Serializer serializer;

    public HttpServerHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {

        httpServerRequest.bodyHandler(body -> {
            // 获取请求
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 结果
            RpcResponse rpcResponse = new RpcResponse();
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpc request is null");
                doResponse(httpServerRequest, rpcResponse);
                return;
            }

            try {
                // 反射调用
                Class<?> serviceClass = LocalRegistry.getService(rpcRequest.getServiceName());
                Method method = serviceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(serviceClass.getDeclaredConstructor().newInstance(), rpcRequest.getParameters());

                // 封装结果
                rpcResponse.setMessage("success");
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setException(e);
                rpcResponse.setMessage(e.getMessage());
            }

            doResponse(httpServerRequest, rpcResponse);
        });
    }

    private void doResponse(HttpServerRequest httpServerRequest, RpcResponse rpcResponse) {
        httpServerRequest.response().putHeader("content-type", serializer.contentType());

        try {
            byte[] responseBytes = serializer.serialize(rpcResponse);
            httpServerRequest.response().end(io.vertx.core.buffer.Buffer.buffer(responseBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
