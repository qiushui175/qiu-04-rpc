package com.qiu.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.qiu.rpc.constant.RpcConstant.*;

/**
 * @author qiu
 * @version 1.0
 * @className RpcRequest
 * @packageName com.qiu.rpc.model
 * @Description
 * @date 2026/1/12 16:04
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 31687997067227778L;

    private String serviceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;

    @Builder.Default
    private String serviceVersion = DEFAULT_SERVICE_VERSION;

    @Builder.Default
    private String serviceGroup = DEFAULT_SERVICE_GROUP;
}
