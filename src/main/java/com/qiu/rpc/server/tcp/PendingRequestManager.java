package com.qiu.rpc.server.tcp;

import com.qiu.rpc.model.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.0
 * @className PendingRequestManager
 * @packageName com.qiu.rpc.server.tcp
 * @Description
 * @date 2026/3/12 12:31
 * @since 1.0
 */
// 全局管理等待中的请求
public class PendingRequestManager {
    public static final ConcurrentHashMap<Long, CompletableFuture<RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();
}