package com.qiu.rpc.serializer;

import lombok.Getter;

/**
 * @author qiu
 * @version 1.0
 * @className SerializerKeys
 * @packageName com.qiu.rpc.constant
 * @Description
 * @date 2026/1/13 17:40
 * @since 1.0
 */
@Getter
public enum SerializerKeys {
    HESSIAN("hessian"),
    JSON("json"),
    KRYO("kryo");

    private final String key;

    SerializerKeys(String key) {
        this.key = key;
    }

    public static SerializerKeys getByKey(String key) {
        if (key != null) {
            key = key.toLowerCase();
            for (SerializerKeys serializerKey : SerializerKeys.values()) {
                if (serializerKey.getKey().equals(key)) {
                    return serializerKey;
                }
            }
        }

        return null;
    }
}
