package com.qiu.rpc.protocol;

import com.qiu.rpc.protocol.buffer.DirectBufferAllocator;
import com.qiu.rpc.serializer.Serializer;
import com.qiu.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageEncoder {

    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {

        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();

        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByCode(header.getSerializationType());
        if (serializerEnum == null) {
            throw new IOException("Unsupported serialization type: " + header.getSerializationType());
        }
        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getName().toLowerCase());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());

        // 使用 Direct Memory 零拷贝 Buffer，预分配精确容量，减少内核态与用户态数据拷贝
        Buffer buffer = DirectBufferAllocator.allocate(ProtocolConstant.MESSAGE_HEADER_LENGTH + bodyBytes.length);
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializationType());
        buffer.appendByte(header.getMessageType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        return buffer;

    }


}
