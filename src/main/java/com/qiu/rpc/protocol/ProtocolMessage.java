package com.qiu.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiu
 * @version 1.0
 * @className ProtocolMessage
 * @packageName com.qiu.rpc.protocol
 * @Description
 * @date 2026/1/19 15:38
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {

    private Header header;

    private T body;

    @Data
    public static class Header {
        private byte magic; // 魔数
        private byte version; // 版本号
        private byte serializationType; // 序列化类型
        private byte messageType; // 消息类型
        private byte status; // 状态
        private long requestId; // 请求ID
        private int bodyLength; // 消息体长度
    }

}
