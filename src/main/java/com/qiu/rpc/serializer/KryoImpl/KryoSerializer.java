package com.qiu.rpc.serializer.KryoImpl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.qiu.rpc.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author qiu
 * @version 1.0
 * @className KryoSerializer
 * @packageName com.qiu.rpc.serializer
 * @Description
 * @date 2026/1/12 15:54
 * @since 1.0
 */
public class KryoSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        Kryo kryo = KryoHolder.get();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, object);

        output.close();
        return outputStream.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        Kryo kryo = KryoHolder.get();

        Input input = new Input(new ByteArrayInputStream(bytes));
        Object o = kryo.readClassAndObject(input);

        input.close();
        return (T) o;
    }

    @Override
    public String contentType() {
        return "application/x-kryo";
    }
}
