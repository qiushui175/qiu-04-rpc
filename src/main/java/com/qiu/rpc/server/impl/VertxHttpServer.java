package com.qiu.rpc.server.impl;

import com.qiu.rpc.serializer.KryoImpl.KryoSerializer;
import com.qiu.rpc.server.HttpServer;
import com.qiu.rpc.server.handler.HttpServerHandler;
import io.vertx.core.Vertx;

/**
 * @author qiu
 * @version 1.0
 * @className VertxHttpServer
 * @packageName com.qiu.rpc.server.impl
 * @Description
 * @date 2026/1/9 15:48
 * @since 1.0
 */
public class VertxHttpServer implements HttpServer {
    @Override
    public void doStart(int port) {

        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        // 创建 HTTP 服务器
        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();

        // 处理请求
//        httpServer.requestHandler(request -> {
//            // 在这里可以添加处理逻辑
//            System.out.println("----------------------------------------------------");
//            System.out.println("Received request uri: " + request.uri());
//            System.out.println("Received request method: " + request.method());
//
//            // 处理传入的请求
//            request.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Hello from Vert.x HTTP Server!");
//        });

        httpServer.requestHandler(new HttpServerHandler(new KryoSerializer()));

        // 监听指定端口
        httpServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port " + port);
            } else {
                System.err.println("Failed to start HTTP server: " + result.cause());
            }
        });
    }
}
