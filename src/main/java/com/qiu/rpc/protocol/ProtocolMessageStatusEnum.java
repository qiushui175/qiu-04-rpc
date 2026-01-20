package com.qiu.rpc.protocol;

import lombok.Getter;

@Getter
public enum ProtocolMessageStatusEnum {

    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50),
    SERVICE_NOT_FOUND("serviceNotFound", 60),
    SERVER_ERROR("serverError", 70),
    CLIENT_ERROR("clientError", 80);

    private final String text;
    private final int code;

    ProtocolMessageStatusEnum(String text, int code) {
        this.text = text;
        this.code = code;
    }

    public static ProtocolMessageStatusEnum getEnumByCode(int code) {
        for (ProtocolMessageStatusEnum statusEnum : ProtocolMessageStatusEnum.values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }
}
