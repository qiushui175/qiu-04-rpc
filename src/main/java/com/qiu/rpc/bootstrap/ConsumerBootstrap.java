package com.qiu.rpc.bootstrap;

import com.qiu.rpc.RpcApplication;

/**
 * @author qiu
 * @version 1.0
 * @className ConsumerBootstrap
 * @packageName com.qiu.rpc.bootstrap
 * @Description
 * @date 2026/1/21 12:20
 * @since 1.0
 */
public class ConsumerBootstrap {

    public static void init() {
        RpcApplication.init("consumer");
    }

}
