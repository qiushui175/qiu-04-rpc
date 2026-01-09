package com.qiu.rpc.server;

/**
 * @author qiu
 * @version 1.0
 * @className HttpServer
 * @packageName com.qiu.rpc.server
 * @Description
 * @date 2026/1/9 15:47
 * @since 1.0
 */
public interface HttpServer {

    /**
     * 启动服务器
     * @param port 端口号
     */
    void doStart(int port);
}
