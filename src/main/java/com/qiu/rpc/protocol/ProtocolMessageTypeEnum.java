package com.qiu.rpc.protocol;

import lombok.Getter;

@Getter
public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEARTBEAT(2),
    OTHERS(3);

    private final int type;

    ProtocolMessageTypeEnum(int type) {
        this.type = type;
    }

    public static ProtocolMessageTypeEnum getByType(int type) {
        for (ProtocolMessageTypeEnum messageTypeEnum : values()) {
            if (messageTypeEnum.getType() == type) {
                return messageTypeEnum;
            }
        }
        return null;
    }
}
