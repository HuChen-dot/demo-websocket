package com.hu.spring.common.pojo;

import lombok.Data;

/**
 * 返回给客户端的响应体
 */
@Data
public class Result {
    private static final long serialVersionUID = -6579929236881323376L;

    /**
     * 系统来源
     */
    private String sourceSystem;

    /**
     * 消息来源用户（消息谁发过来的）
     */
    private String sourceUserId;


    /**
     * 目标用户，这条消息发送给谁
     */
    private String toUserId;

    /**
     * 事件类型（可以用来区分这条消息用来干嘛，比如：
     *  设备异常上报类型，可以做相应的服务端保存异常信息逻辑，等等。。
     *  比如聊天消息类型，可以将这条消息转发给对应的人的逻辑
     *  。。。。等等，可以根据这个类型做不同的操作
     */
    private String eventType;

    /**
     * 消息类型,区分是文本消息还是二进制消息，等等。。。
     * 在枚举类：MsgTypeEnum
     * 默认：文本消息：text
     */
    private String msgType = "text";

    /**
     * 消息内容
     */
    private Object msg;


    public static Result alert(String msg){
        Result result = new Result();
        result.setEventType("alert");
        result.setMsgType(MsgTypeEnum.TEXT.getType());
        result.setMsg(msg);
        return result;
    }

    public static Result toResult(MessageDto dto){
        Result result = new Result();
        result.setSourceSystem(dto.getSourceSystem());
        result.setSourceUserId(dto.getUserId());
        result.setToUserId(dto.getToUserId());
        result.setMsg(dto.getMsg());
        result.setMsgType(dto.getMsgType());
        result.setEventType(dto.getEventType());
        return result;
    }

    public static Result toResult(MessageParam messageParam){
        Result result = new Result();
        result.setSourceSystem(messageParam.getSourceSystem());
        result.setSourceUserId(messageParam.getUserId());
        result.setToUserId(messageParam.getToUserId());
        result.setMsg(messageParam.getMsg());
        result.setEventType(messageParam.getEventType());
        result.setMsgType(messageParam.getMsgType());
        return result;
    }

}
