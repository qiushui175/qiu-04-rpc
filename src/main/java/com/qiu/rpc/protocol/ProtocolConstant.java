package com.qiu.rpc.protocol;

public interface ProtocolConstant {

    // 消息头长度
    int MESSAGE_HEADER_LENGTH = 17;

    // 魔数
    byte PROTOCOL_MAGIC = (byte) 0xBC;

    // 版本号
    byte PROTOCOL_VERSION = 0x1;

}
