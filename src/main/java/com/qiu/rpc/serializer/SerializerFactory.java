package com.qiu.rpc.serializer;

import com.qiu.rpc.serializer.HessianImpl.HessianSerializer;
import com.qiu.rpc.serializer.JsonImpl.JsonSerializer;
import com.qiu.rpc.serializer.KryoImpl.KryoSerializer;
import com.qiu.rpc.spi.SpiLoader;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author qiu
 * @version 1.0
 * @className SerializerFactory
 * @packageName com.qiu.rpc.serializer
 * @Description
 * @date 2026/1/13 17:41
 * @since 1.0
 */
public class SerializerFactory {
    /*private static final Map<SerializerKeys, Serializer> SERIALIZER_MAP =
            new EnumMap<>(SerializerKeys.class);

    static {
        SERIALIZER_MAP.put(SerializerKeys.JSON, new JsonSerializer());
        SERIALIZER_MAP.put(SerializerKeys.HESSIAN, new HessianSerializer());
        SERIALIZER_MAP.put(SerializerKeys.KRYO, new KryoSerializer());
    }

    public static Serializer getSerializer(SerializerKeys key) {
        Serializer serializer = SERIALIZER_MAP.get(key);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown serializer key: " + key);
        }
        return serializer;
    }

    public static Serializer getSerializer(String key) {
        SerializerKeys serializerKey = SerializerKeys.getByKey(key);
        if (serializerKey == null) {
            throw new IllegalArgumentException("Unknown serializer key: " + key);
        }
        return getSerializer(serializerKey);
    }*/

    static {
        SpiLoader.load(Serializer.class);
    }

    public static Serializer getSerializer(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
