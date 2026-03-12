package com.qiu.rpc.server.tcp;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TcpConnectionManager {

    /**
     * 每个服务的连接数 —— 压测时建议提高到 8~16
     */
    private static final int CONNECTION_COUNT = 8;

    /**
     * 对每个连接维护一个写入锁，防止并发 write 导致字节流交错
     */
    private static final Map<String, List<Object>> WRITE_LOCKS = new ConcurrentHashMap<>();

    private static final Map<String, List<CompletableFuture<NetSocket>>> POOL =
            new ConcurrentHashMap<>();

    private static final Map<String, AtomicInteger> INDEX =
            new ConcurrentHashMap<>();

    /**
     * 获取连接以及对应的写入锁
     */
    public static ConnectionWrapper getConnection(String host, int port) {
        String key = host + ":" + port;

        // 使用 synchronized 保证连接池初始化的原子性
        List<CompletableFuture<NetSocket>> list = POOL.get(key);
        if (list == null) {
            synchronized (POOL) {
                list = POOL.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    List<Object> locks = new ArrayList<>();
                    for (int i = 0; i < CONNECTION_COUNT; i++) {
                        list.add(createConnection(host, port));
                        locks.add(new Object());
                    }
                    POOL.put(key, list);
                    WRITE_LOCKS.put(key, locks);
                    INDEX.put(key, new AtomicInteger());
                }
            }
        }

        // 修复溢出问题：用位运算取正数
        int idx = (INDEX.get(key).getAndIncrement() & 0x7FFFFFFF) % CONNECTION_COUNT;

        return new ConnectionWrapper(list.get(idx), WRITE_LOCKS.get(key).get(idx));
    }

    /**
     * 封装连接和对应的写入锁
     */
    public static class ConnectionWrapper {
        private final CompletableFuture<NetSocket> socketFuture;
        private final Object writeLock;

        public ConnectionWrapper(CompletableFuture<NetSocket> socketFuture, Object writeLock) {
            this.socketFuture = socketFuture;
            this.writeLock = writeLock;
        }

        public CompletableFuture<NetSocket> getSocketFuture() {
            return socketFuture;
        }

        /**
         * 线程安全的写入方法 —— 保证一个完整消息的字节不会被其他线程打断
         */
        public void safeWrite(NetSocket socket, Buffer data) {
            synchronized (writeLock) {
                socket.write(data);
            }
        }
    }

    private static CompletableFuture<NetSocket> createConnection(String host, int port) {
        CompletableFuture<NetSocket> future = new CompletableFuture<>();
        NetClient client = VertxTcpFactory.getNetClientInstance();

        client.connect(port, host, res -> {
            if (!res.succeeded()) {
                future.completeExceptionally(res.cause());
                return;
            }
            NetSocket socket = res.result();
            log.info("TCP连接建立 {}:{}", host, port);
            TcpResponseHandler.init(socket);
            future.complete(socket);
        });

        return future;
    }
}