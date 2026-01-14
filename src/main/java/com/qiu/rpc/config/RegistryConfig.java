package com.qiu.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author qiu
 * @version 1.0
 * @className RegistryConfig
 * @packageName com.qiu.rpc.config
 * @Description
 * @date 2026/1/13 21:35
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistryConfig {

    private String registry = "etcd";

    private String address = "http://localhost:2379";

    private String username;

    private String password;

    private Long timeout = 10000L;

}
