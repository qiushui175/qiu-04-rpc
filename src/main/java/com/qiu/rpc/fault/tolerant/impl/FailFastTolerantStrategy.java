package com.qiu.rpc.fault.tolerant.impl;

import com.qiu.rpc.fault.tolerant.TolerantStrategy;
import com.qiu.rpc.model.RpcResponse;

import java.util.Map;

/**
 * @author qiu
 * @version 1.0
 * @className FailFastTolerantStrategy
 * @packageName com.qiu.rpc.fault.tolerant.impl
 * @Description
 * @date 2026/1/21 11:49
 * @since 1.0
 */
public class FailFastTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("Fail fast tolerant strategy triggered. No retries will be attempted.", e);
    }
}
