package com.qiu.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiu
 * @version 1.0
 * @className ServiceRegisterInfo
 * @packageName com.qiu.rpc.model
 * @Description
 * @date 2026/1/21 12:11
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegisterInfo<T> {

    private String serviceName;

    private Class<? extends T> implClass;

}
