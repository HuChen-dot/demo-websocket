package com.hu.spring.common.event;


import com.hu.spring.common.pojo.MessageDto;
import org.springframework.web.socket.WebSocketSession;

public interface EventInterface {


    /**
     * 处理消息,本方法禁止抛出异常，因为抛出异常后会导致和客户端的连接断开
     * @param messageDto
     */
    void handle(WebSocketSession session, MessageDto messageDto);
}
