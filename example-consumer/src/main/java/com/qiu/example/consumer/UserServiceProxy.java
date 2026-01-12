package com.qiu.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiu.example.common.model.User;
import com.qiu.example.common.service.UserService;
import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.serializer.KryoImpl.KryoSerializer;
import com.qiu.rpc.serializer.Serializer;

/**
 * @author qiu
 * @version 1.0
 * @className UserServiceProxy
 * @packageName com.qiu.example.consumer
 * @Description
 * @date 2026/1/12 16:38
 * @since 1.0
 */
public class UserServiceProxy implements UserService {
    @Override
    public User getUser(Integer id) {
        Serializer serializer = new KryoSerializer();

        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class<?>[]{Integer.class})
                .parameters(new Object[]{id})
                .build();

        try {
            byte[] bytes = serializer.serialize(rpcRequest);
            byte[] res;

            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:18080")
                    .body(bytes)
                    .execute()) {
                res = httpResponse.bodyBytes();
            }

            RpcResponse response = serializer.deserialize(res, RpcResponse.class);
            return (User) response.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
