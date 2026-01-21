package com.qiu.rpc.fault.tolerant.impl;

import com.qiu.rpc.fault.tolerant.TolerantStrategy;
import com.qiu.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author qiu
 * @version 1.0
 * @className FailSafeTolerantStrategy
 * @packageName com.qiu.rpc.fault.tolerant.impl
 * @Description
 * @date 2026/1/21 11:50
 * @since 1.0
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("Fail safe tolerant strategy triggered. Exception: {}", e.getMessage());
        return new RpcResponse();
    }
}
