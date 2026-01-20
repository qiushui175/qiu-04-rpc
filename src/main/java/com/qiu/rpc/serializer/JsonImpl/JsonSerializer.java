package com.qiu.rpc.serializer.JsonImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.serializer.Serializer;

import java.io.IOException;

/**
 * @author qiu
 * @version 1.0
 * @className JsonSerializer
 * @packageName com.qiu.rpc.serializer.JsonImpl
 * @Description
 * @date 2026/1/13 17:14
 * @since 1.0
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        if (object == null) {
            return new byte[0];
        }
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        // 先做一次“普通反序列化”
        Object obj = OBJECT_MAPPER.readValue(bytes, clazz);

        // ===== RPC 特殊处理开始 =====

        if (obj instanceof RpcRequest) {
            handleRpcRequest((RpcRequest) obj);
        }

        if (obj instanceof RpcResponse) {
            handleRpcResponse((RpcResponse) obj);
        }

        // ===== RPC 特殊处理结束 =====

        return (T) obj;
    }

    @Override
    public String contentType() {
        return "application/json";
    }

    private void handleRpcRequest(RpcRequest request) {
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();

        if (parameters == null || parameterTypes == null) {
            return;
        }

        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            Class<?> targetType = parameterTypes[i];

            if (param != null && !targetType.isAssignableFrom(param.getClass())) {
                // LinkedHashMap / ArrayList → 目标参数类型
                Object converted = OBJECT_MAPPER.convertValue(param, targetType);
                parameters[i] = converted;
            }
        }
    }

    private void handleRpcResponse(RpcResponse response) {
        Object data = response.getData();
        Class<?> dataType = response.getDataType();

        if (data == null || dataType == null) {
            return;
        }

        if (!dataType.isAssignableFrom(data.getClass())) {
            Object converted = OBJECT_MAPPER.convertValue(data, dataType);
            response.setData(converted);
        }
    }
}
