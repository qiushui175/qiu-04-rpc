package com.qiu.rpc.protocol;

import com.qiu.rpc.model.RpcRequest;
import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.serializer.Serializer;
import com.qiu.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageDecoder {

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        if (buffer == null || buffer.length() < 17) { // 最小头部 + bodyLength 字节数
            throw new IOException("Insufficient data to decode ProtocolMessage");
        }

        int pos = 0;
        byte magic = buffer.getByte(pos++);
        byte version = buffer.getByte(pos++);
        byte serializationType = buffer.getByte(pos++);
        byte messageType = buffer.getByte(pos++);
        byte status = buffer.getByte(pos++);
        long requestId = buffer.getLong(pos);
        pos += 8;
        int bodyLen = buffer.getInt(pos);
        pos += 4;

        if (bodyLen < 0 || buffer.length() - pos < bodyLen) {
            throw new IOException("Invalid or incomplete body data");
        }

        byte[] bodyBytes = buffer.getBytes(pos, pos + bodyLen);

        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerializationType(serializationType);
        header.setMessageType(messageType);
        header.setStatus(status);
        header.setRequestId(requestId);

        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByCode(header.getSerializationType());
        if (serializerEnum == null) {
            throw new IOException("Unsupported serialization type: " + header.getSerializationType());
        }
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getByType(messageType);
        if (messageTypeEnum == null) {
            throw new IOException("Unsupported message type: " + messageType);
        }

        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getName());
        Object body = null;
        if (bodyLen > 0) {
            switch (messageTypeEnum) {
                case REQUEST:
                    body = serializer.deserialize(bodyBytes, RpcRequest.class);
                    break;
                case RESPONSE:
                    body = serializer.deserialize(bodyBytes, RpcResponse.class);
                    break;
                case HEARTBEAT:
                default:
                    throw new RuntimeException("Unsupported message type: " + messageType);
            }
        }

        return new ProtocolMessage<>(header, body);

    }

}
