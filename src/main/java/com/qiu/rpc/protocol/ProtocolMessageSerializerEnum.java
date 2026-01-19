package com.qiu.rpc.protocol;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ProtocolMessageSerializerEnum {

    KRYO((byte) 0x01, "KRYO"),
    JSON((byte) 0x02, "JSON"),
    PROTOSTUFF((byte) 0x03, "PROTOSTUFF");

    private final byte code;
    private final String name;

    ProtocolMessageSerializerEnum(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    // Get enum by code
    public static ProtocolMessageSerializerEnum getByCode(byte code) {
        for (ProtocolMessageSerializerEnum serializerEnum : values()) {
            if (serializerEnum.getCode() == code) {
                return serializerEnum;
            }
        }
        return null;
    }

    public static List<String> getAllNames() {
        return Arrays.stream(values()).map(item -> item.name).toList();
    }

    public static ProtocolMessageSerializerEnum getByName(String name) {
        if (ObjUtil.isEmpty(name)) return null;
        for (ProtocolMessageSerializerEnum serializerEnum : values()) {
            if (serializerEnum.getName().equalsIgnoreCase(name)) {
                return serializerEnum;
            }
        }
        return null;
    }
}
