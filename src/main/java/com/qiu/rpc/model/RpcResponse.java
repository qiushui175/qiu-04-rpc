package com.qiu.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiu
 * @version 1.0
 * @className RpcResponse
 * @packageName com.qiu.rpc.model
 * @Description
 * @date 2026/1/12 16:04
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse {

    private Object data;

    private Class<?> dataType;

    private String message;

    private Exception exception;
}
