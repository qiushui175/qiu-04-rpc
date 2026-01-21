package com.qiu.rpc.fault.tolerant;

import com.qiu.rpc.model.RpcResponse;

import java.util.Map;

/**
 * @author qiu
 * @version 1.0
 * @className TolerantStrategy
 * @packageName com.qiu.rpc.fault.tolerant
 * @Description
 * @date 2026/1/21 11:47
 * @since 1.0
 */
public interface TolerantStrategy {
    RpcResponse doTolerant(Map<String, Object> context, Exception e);

}
