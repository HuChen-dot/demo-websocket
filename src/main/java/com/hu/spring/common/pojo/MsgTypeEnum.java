package com.hu.spring.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */

@Getter
@AllArgsConstructor
public enum MsgTypeEnum {

    TEXT("text", "文本消息"),
    BYTE("byte", "二进制消息"),
    ;

    private String type;
    private String name;

    public static String getName(String type) {
        MsgTypeEnum[] values = MsgTypeEnum.values();
        for (MsgTypeEnum value : values) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return "";
    }


}
