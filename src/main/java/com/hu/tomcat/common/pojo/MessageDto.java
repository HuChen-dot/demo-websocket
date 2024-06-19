package com.hu.tomcat.common.pojo;

import lombok.Data;
/**
 * 用于消息在集群内部传递消息使用
 */
@Data
public class MessageDto extends MessageParam{
    private static final long serialVersionUID = -6579929236881323376L;

    /**
     * 客户端标识，可以是客户端IP
     */
    private String clientSign;


    /**
     * 暂时存储真正的消息类型
     */
    private String itemEventType;
}
