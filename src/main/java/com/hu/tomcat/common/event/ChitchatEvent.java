package com.hu.tomcat.common.event;

import com.hu.tomcat.common.pojo.MessageDto;
import com.hu.tomcat.common.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

/**
 * 处理聊天类型的消息
 */
@Component
public class ChitchatEvent extends AbstractEvent {


    @Autowired
    private MessageService messageService;


    /**
     * 处理消息,本方法禁止抛出异常，因为抛出异常后会导致和客户端的连接断开
     * @param messageDto
     */
    @Override
    public void handle(Session session, MessageDto messageDto) {
        // 将消息进行发送
        messageService.sendToUser(messageDto);
    }


    @Override
    public String eventType() {
        return "message_forwarding";
    }
}
