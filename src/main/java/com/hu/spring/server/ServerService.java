package com.hu.spring.server;

import com.hu.spring.common.pojo.Constant;
import com.hu.spring.common.pojo.UserInfo;
import com.hu.spring.common.service.MessageService;
import com.hu.util.LocalIpUtils;
import com.hu.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ServerService implements WebSocketHandler {

    @Autowired
    private RedisUtils redisUtils;


    @Autowired
    private MessageService messageService;

    /**
     *  当WebSocket建立连接成功后会触发这个注解修饰的方法。
     * @param session 连接
     * @param-sourceSystem 系统来源（比如：wison-punch  或者 wison-scm
     * @param-clientSign 客户端标识，可以是客户端ip
     * @param-userInfo 用户信息，在拦截器中添加到里面的
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        String sourceSystem = (String) attributes.get("sourceSystem");
        String clientSign = (String) attributes.get("clientSign");
        UserInfo user = (UserInfo) attributes.get("userInfo");


        // 非集群内其他客户端建立的连接才进行以下操作
        if (!"cluster".equals(sourceSystem)){
            // 查询是否已经存在链接，支持一个账号多地登录，但是禁止一个账号同一个客户端多次登录
            String server = redisUtils.get(Constant.USER_SERVER_REGISTER_KEY + user.getUserKey());
            if (server != null) {
                // 代表已存在链接,将之前的连接关闭
                messageService.close(user.getUserKey());
            }
            // 将当前用户存在的客户端ip进行存储，一个用户可能会在多个客户端登陆
            redisUtils.setForSet(Constant.USER_CLIENT_SIGN_KEY + user.getUserId() + sourceSystem, Arrays.asList(clientSign));

            // 将当前用户和当前服务器进行绑定,放进分布式存储中，用于用户消息转发时找到目标服务器
            redisUtils.set(Constant.USER_SERVER_REGISTER_KEY + user.getUserKey(), LocalIpUtils.getIntranetIP() + ":" + messageService.port);
        }

        // 将用户和session绑定
        Constant.USERID_SESSION.put(user.getUserKey(), session);

        // 将session和用户绑定，方便通过session查找用户
        Constant.SESSION_USERID.put(session.getId(), user.getUserKey());


    }


    /**
     * 接收消息
     *
     * @param session Session
     * @param message 消息
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
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
     * 关闭连接
     *
     * @param session     Session
     * @param closeStatus 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userKey = Constant.SESSION_USERID.get(session.getId());
        Constant.USERID_SESSION.remove(userKey);

        Set<Object> clientSignLIst = redisUtils.getForSetAll(Constant.USER_CLIENT_SIGN_KEY + userKey.split("_")[0]);
        if (clientSignLIst != null) {
            for (Object clientSign : clientSignLIst) {
                if (userKey.indexOf(String.valueOf(clientSign)) != -1) {
                    redisUtils.delForSet(Constant.USER_CLIENT_SIGN_KEY + userKey.split("_")[0], clientSign);
                    break;
                }
            }
        }

        Constant.SESSION_USERID.remove(session.getId());
        redisUtils.delete(Constant.USER_SERVER_REGISTER_KEY + userKey);
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
