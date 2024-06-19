package com.hu.tomcat.client;

import com.hu.tomcat.common.pojo.Constant;
import com.hu.tomcat.common.service.MessageService;
import com.hu.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;

@ClientEndpoint
@Slf4j
public class ClientService {

    private String userId;

    public ClientService(String userId) {
        this.userId = userId;
    }

    /**
     * 打开链接
     *
     * @param session
     */
    @OnOpen
    public void open(Session session) {
        log.info("Client WebSocket is opening...");
        String userKey = userId;
        // 将session和用户绑定
        Constant.SESSION_USERID.put(session.getId(), userKey);

        // 将用户和session绑定
        Constant.USERID_SESSION.put(userKey, session);
    }


    /**
     * 接收到来自服务端的消息
     *
     * @param message
     */
    @OnMessage
    public void onMessage(Session session,String message) throws Exception {
        log.info("Server send message: " + message);
        //执行逻辑,处理消息
        MessageService messageService = SpringContextUtils.getBean(MessageService.class);
        messageService.handle(session,message);
    }

    /**
     * 链接关闭事件
     */
    @OnClose
    public void onClose(Session session) {
        log.info("Websocket closed");

        String userKey = Constant.SESSION_USERID.get(session.getId());
        Constant.USERID_SESSION.remove(userKey);
        Constant.SESSION_USERID.remove(session.getId());
    }


}
