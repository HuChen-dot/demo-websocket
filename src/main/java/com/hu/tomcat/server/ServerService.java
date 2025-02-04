package com.hu.tomcat.server;

import com.hu.tomcat.common.pojo.Constant;
import com.hu.tomcat.common.service.MessageService;
import com.hu.util.IpUtil;
import com.hu.util.RedisUtils;
import com.hu.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Arrays;
import java.util.Set;

/**
 * 服务端通用websocket处理类
 */
@Slf4j
@ServerEndpoint("/globalWs/{sourceSystem}/{clientSign}/{userId}")
@Component
public class ServerService {

    private RedisUtils redisUtils = SpringContextUtils.getBean(RedisUtils.class);

    private MessageService messageService = SpringContextUtils.getBean(MessageService.class);

    /**
     * 当WebSocket建立连接成功后会触发这个注解修饰的方法。
     *
     * @param session
     * @param userId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sourceSystem") String sourceSystem, @PathParam("clientSign") String clientSign, @PathParam("userId") String userId) {
        String userKey = userId;
        // 非集群内其他客户端建立的连接才进行以下操作
        if (!"cluster".equals(sourceSystem)){
            userKey = userId + sourceSystem + "_" + clientSign;
            // 查询是否已经存在链接，支持一个账号多地登录，但是禁止一个账号同一个客户端多次登录
            String server = redisUtils.get(Constant.USER_SERVER_REGISTER_KEY + userKey);
            if (server != null) {
                // 代表已存在链接,将之前的连接关闭
                messageService.close(userKey);
            }

            // 将当前用户存在的客户端ip进行存储，一个用户可能会在多个客户端登陆
            redisUtils.setForSet(Constant.USER_CLIENT_SIGN_KEY + userId + sourceSystem, Arrays.asList(clientSign));
            // 将当前用户和当前服务器进行绑定,放进分布式存储中，用于用户消息转发时找到目标服务器
            redisUtils.set(Constant.USER_SERVER_REGISTER_KEY + userKey, IpUtil.getIp() + ":" + messageService.port);
        }

        // 将用户和session绑定
        Constant.USERID_SESSION.put(userKey, session);

        // 将session和用户绑定，方便通过session查找用户
        Constant.SESSION_USERID.put(session.getId(), userKey);
    }


    /**
     * 当客户端发送消息到服务端时，会触发这个注解修改的方法
     *
     * @param session
     * @param message
     * @return
     */
    @OnMessage
    public String onMessage(Session session, String message) throws Exception {
        //执行逻辑,处理消息
        messageService.handle(session, message);
        return null;
    }


    /**
     * 当WebSocket建立连接时出现异常会触发这个注解修饰的方法。
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("ws session 发生错误:{}", throwable.getMessage());
    }

    /**
     * 当WebSocket建立的连接断开后会触发这个注解修饰的方法
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
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


}
