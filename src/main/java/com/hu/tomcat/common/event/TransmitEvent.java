package com.hu.tomcat.common.event;

import com.hu.tomcat.common.pojo.Constant;
import com.hu.tomcat.common.pojo.MessageDto;
import com.hu.tomcat.common.pojo.Result;
import com.hu.tomcat.common.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

/**
 * 处理服务端转发过来的消息
 */
@Component
@Slf4j
public class TransmitEvent extends AbstractEvent {


    @Autowired
    private MessageService messageService;

    /**
     * 处理消息,本方法禁止抛出异常，因为抛出异常后会导致和客户端的连接断开
     * @param messageDto
     */
    @Override
    public void handle(Session session, MessageDto messageDto) {
        // 将消息进行发送
        // 从本地查找链接
        String userKey = messageDto.getToUserId() + messageDto.getSourceSystem() + "_" + messageDto.getClientSign();
        Session toSession = Constant.USERID_SESSION.get(userKey);
        if (toSession != null) {
            try {
                messageDto.setEventType(messageDto.getItemEventType());

                messageService.sendMessage(toSession, Result.toResult(messageDto));
            } catch (Exception e) {
                log.error("发送消息异常", e);
            }
        }
    }


    @Override
    public String eventType() {
        return "transmit";
    }
}
