package com.qiu.rpc.serializer;

import java.io.IOException;

/**
 * @author qiu
 * @version 1.0
 * @className Serializer
 * @packageName com.qiu.rpc.serializer
 * @Description
 * @date 2026/1/12 15:51
 * @since 1.0
 */
public interface Serializer {

    /**
     * Serialize an object to a byte array
     * @param object
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * Deserialize a byte array to an object
     * @param bytes
     * @param clazz
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;

    String contentType();
}
