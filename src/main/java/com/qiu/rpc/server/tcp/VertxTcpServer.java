package com.qiu.rpc.server.tcp;

import com.qiu.rpc.server.HttpServer;
import com.qiu.rpc.server.handler.TcpServerHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxTcpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();

        // TCP server implementation would go here
        NetServer netServer = vertx.createNetServer();

//        netServer.connectHandler(netSocket -> {
//            netSocket.handler(buffer -> {
//                byte[] requestData = buffer.getBytes();
//
//                byte[] responseData = handleRequest(requestData);
//
//                netSocket.write(Buffer.buffer(responseData));
//            });
//        });
        netServer.connectHandler(new TcpServerHandler());
        log.info("start listen");
        netServer.listen(port, res -> {
            if (res.succeeded()) {
                log.info("TCP server is now listening on port " + port);
            } else {
                log.info("Failed to bind TCP server on port " + port + ": " + res.cause().getMessage());
            }
        });
    }

    private byte[] handleRequest(byte[] requestData) {
        // 在这里对数据进行处理
        // exp
        log.info("Received request: " + new String(requestData));
        return "hello client".getBytes();
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(18888);
    }
}
