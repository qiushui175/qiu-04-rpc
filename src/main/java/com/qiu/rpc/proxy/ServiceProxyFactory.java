package com.qiu.rpc.proxy;

import com.qiu.rpc.serializer.KryoImpl.KryoSerializer;

import java.lang.reflect.Proxy;

/**
 * @author qiu
 * @version 1.0
 * @className ServiceProxyFactory
 * @packageName com.qiu.rpc.proxy
 * @Description
 * @date 2026/1/12 16:49
 * @since 1.0
 */
public class ServiceProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy(new KryoSerializer()));
    }

}
