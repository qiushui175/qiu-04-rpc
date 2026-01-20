package com.qiu.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxTcpClient {

    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(port, "localhost", res -> {
            if (res.succeeded()) {
                // 发送请求数据
                NetSocket socket = res.result();
                socket.write("Hello Server");

                // 处理响应数据
                socket.handler(buffer -> {
                    byte[] responseData = buffer.getBytes();
                   log.info("Received response: " + new String(responseData));
                });
            } else {
               log.info("Failed to connect to server: " + res.cause().getMessage());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().doStart(18888);
    }
}
