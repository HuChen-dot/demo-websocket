package com.hu.tomcat.common.event;


import com.hu.tomcat.common.pojo.MessageDto;

import javax.websocket.Session;

public interface EventInterface {


    /**
     * 处理消息,本方法禁止抛出异常，因为抛出异常后会导致和客户端的连接断开
     * @param messageDto
     */
    void handle(Session session, MessageDto messageDto);
}
