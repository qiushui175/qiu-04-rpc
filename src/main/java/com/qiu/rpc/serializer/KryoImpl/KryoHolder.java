package com.qiu.rpc.serializer.KryoImpl;

import com.esotericsoftware.kryo.Kryo;

/**
 * @author qiu
 * @version 1.0
 * @className KryoHolder
 * @packageName com.qiu.rpc.serializer.KryoImpl
 * @Description
 * @date 2026/1/12 15:55
 * @since 1.0
 */
public final class KryoHolder {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> {
                Kryo kryo = new Kryo();

                kryo.setReferences(true);
                kryo.setRegistrationRequired(false);

                return kryo;
            });

    public static Kryo get() {
        return KRYO_THREAD_LOCAL.get();
    }
}
