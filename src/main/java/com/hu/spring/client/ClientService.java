package com.hu.spring.client;


import com.hu.spring.common.pojo.Constant;
import com.hu.spring.common.service.MessageService;
import com.hu.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

@Slf4j
public class ClientService implements WebSocketHandler {

    private String userId;

    public ClientService(String userId) {
        this.userId = userId;
    }

    /**
     * 打开链接
     *
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("Server send message: " + message);
        //执行逻辑,处理消息
        MessageService messageService = SpringContextUtils.getBean(MessageService.class);
        //执行逻辑,处理消息
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            messageService.handle(session, textMessage.getPayload());
        }
    }

    /**
     * 发生错误
     *
     * @param session   Session
     * @param exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("ws session 发生错误:{}", exception.getMessage());
    }


    /**
     * 链接关闭事件
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Websocket closed");
        String userKey = Constant.SESSION_USERID.get(session.getId());
        Constant.USERID_SESSION.remove(userKey);
        Constant.SESSION_USERID.remove(session.getId());
    }


    /**
     * 是否支持接收不完整的消息
     *
     * @return
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
