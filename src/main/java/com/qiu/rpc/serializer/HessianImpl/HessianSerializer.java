package com.qiu.rpc.serializer.HessianImpl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.qiu.rpc.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author qiu
 * @version 1.0
 * @className He
 * @packageName com.qiu.rpc.serializer.HessianImpl
 * @Description
 * @date 2026/1/13 17:38
 * @since 1.0
 */


/**
 * Hessian 序列化器
 */
public class HessianSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        if (object == null) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HessianOutput hessianOutput = new HessianOutput(baos);

        hessianOutput.writeObject(object);
        hessianOutput.flush();

        return baos.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        HessianInput hessianInput = new HessianInput(bais);

        Object obj = hessianInput.readObject();

        return (T) obj;
    }

    @Override
    public String contentType() {
        return "application/x-hessian";
    }
}
