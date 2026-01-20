package com.qiu.rpc.server.handler;

import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.protocol.ProtocolMessage;
import com.qiu.rpc.protocol.ProtocolMessageDecoder;
import com.qiu.rpc.protocol.ProtocolMessageEncoder;
import com.qiu.rpc.protocol.ProtocolMessageTypeEnum;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author qiu
 * @version 1.0
 * @className TcpServerHandler
 * @packageName com.qiu.rpc.server.handler
 * @Description
 * @date 2026/1/20 10:50
 * @since 1.0
 */
@Slf4j
public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        netSocket.handler(buffer -> {
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            RpcResponse response = new RpcResponse();
            try {
                // 反射调用
                RpcRequest rpcRequest = protocolMessage.getBody();
                Class<?> serviceClass = com.qiu.rpc.registry.LocalRegistry.getService(rpcRequest.getServiceName());
                java.lang.reflect.Method method = serviceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(serviceClass.getDeclaredConstructor().newInstance(), rpcRequest.getParameters());

                // 封装结果
                response.setMessage("success");
                response.setData(result);
                response.setDataType(method.getReturnType());
            } catch (Exception e) {
                response.setMessage("error: " + e.getMessage());
                response.setException(e);
            }

            // 发送
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setMessageType((byte) ProtocolMessageTypeEnum.RESPONSE.getType());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, response);
            try {
                Buffer resp = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(resp);
                log.info("Response sent to client");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }
}
