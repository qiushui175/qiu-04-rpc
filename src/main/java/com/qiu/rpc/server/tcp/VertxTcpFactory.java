package com.qiu.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;

/**
 * @author qiu
 * @version 1.0
 * @className VertxFactory
 * @packageName com.qiu.rpc.server
 * @Description
 * @date 2026/1/20 14:57
 * @since 1.0
 */
public class VertxTcpFactory {
    private static volatile Vertx vertxInstance;
    private static volatile NetClient netClientInstance;

    public static Vertx getVertxInstance() {
        if (vertxInstance == null) {
            synchronized (VertxTcpFactory.class) {
                if (vertxInstance == null) {
                    vertxInstance = Vertx.vertx();
                }
            }
        }
        return vertxInstance;
    }

    public static NetClient getNetClientInstance(){
        if (netClientInstance == null) {
            synchronized (VertxTcpFactory.class) {
                if (netClientInstance == null) {
                    netClientInstance = getVertxInstance().createNetClient();
                }
            }
        }
        return netClientInstance;
    }

    public static void shutdown() {
        if (netClientInstance != null) {
            netClientInstance.close();
        }
        if (vertxInstance != null) {
            vertxInstance.close();
        }
    }
}
