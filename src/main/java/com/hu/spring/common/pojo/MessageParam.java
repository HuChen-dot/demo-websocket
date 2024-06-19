package com.hu.spring.common.pojo;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * 用于其他客户端请求服务端的消息体
 */
@Data
public class MessageParam {

    public static void main(String[] args) {

        MessageParam messageParam = new MessageParam();
        messageParam.setSourceSystem("test");
        messageParam.setEventType("message_forwarding");
        messageParam.setToUserId("李四");
        messageParam.setMsg("nihao11");
        messageParam.setUserId("张三");
        System.err.println(JSON.toJSONString(messageParam));

    }

    private static final long serialVersionUID = -6579929236881323376L;


    /**
     * 系统来源
     */
    private String sourceSystem;

    /**
     * 当前用户
     */
    private String userId;


    /**
     * 目标用户，这条消息发送给谁
     */
    private String toUserId;

    /**
     * 消息类型,区分是文本消息还是二进制消息，等等。。。
     * 在枚举类：MsgTypeEnum
     * 默认：文本消息：text
     */
    private String msgType = "text";

    /**
     * 事件类型（可以用来区分这条消息用来干嘛，比如：
     *  设备异常上报类型，可以做相应的服务端保存异常信息逻辑，等等。。
     *  比如聊天消息类型，可以将这条消息转发给对应的人的逻辑
     *  。。。。等等，可以根据这个类型做不同的操作
     */
    private String eventType;

    /**
     * 消息内容
     */
    private Object msg;
}
