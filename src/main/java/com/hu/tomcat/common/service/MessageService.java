package com.hu.tomcat.common.service;

import com.alibaba.fastjson.JSON;
import com.hu.spring.common.pojo.MsgTypeEnum;
import com.hu.tomcat.client.ClientInit;
import com.hu.tomcat.common.event.AbstractEvent;
import com.hu.tomcat.common.event.EventInterface;
import com.hu.tomcat.common.pojo.Constant;
import com.hu.tomcat.common.pojo.MessageDto;
import com.hu.tomcat.common.pojo.MessageParam;
import com.hu.tomcat.common.pojo.Result;
import com.hu.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.websocket.Session;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Service
public class MessageService {


    @Value("${server.port}")
    public String port;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ClientInit clientInit;


    /**
     * 处理消息
     *
     * @param message
     * @return 返回不为空，则将内容返回给客户端展示
     * @throws Exception
     */
    public void handle(Session session, String message) throws Exception {
        // 处理消息
        MessageDto messageDto = null;
        try {
            messageDto = JSON.parseObject(message, MessageDto.class);
        } catch (Exception e) {
            sendMessage(session, Result.alert("消息转换失败，消息内容：" + message));
            return;
        }

        if (StringUtils.isEmpty(messageDto.getEventType())) {
            sendMessage(session, Result.alert("事件类型为空"));
            return;
        }
        EventInterface eventInterface = AbstractEvent.STRATEGY_TABLE.get(messageDto.getEventType());
        if (eventInterface == null) {
            sendMessage(session, Result.alert("未知的事件类型：" + messageDto.getEventType()));
            return;
        }
        eventInterface.handle(session, messageDto);
    }


    /**
     * 向user精确用户发送消息
     * 有可能目标用户的websocket链接不在本服务器，需要进行转发
     *
     * @param messageParam 消息内容
     */
    public void sendToUser(MessageParam messageParam) {
        Set<Object> clientSignLIst = redisUtils.getForSetAll(Constant.USER_CLIENT_SIGN_KEY + messageParam.getToUserId() + messageParam.getSourceSystem());
        if(clientSignLIst == null){
            return;
        }
        for (Object clientSign : clientSignLIst) {
            // 从本地查找链接
            try {
                String userKey = messageParam.getToUserId() + messageParam.getSourceSystem() +"_"+ clientSign;
                Session toSession = Constant.USERID_SESSION.get(userKey);
                if (toSession != null) {
                    sendMessage(toSession, Result.toResult(messageParam));
                    continue;
                }

                // 从分布式存储中获取用户所链接的服务器
                String userInServer = redisUtils.get(Constant.USER_SERVER_REGISTER_KEY + userKey);
                if (userInServer == null) {
                    // todo 代表当前用户不在线，可以先将消息进行存储，等到用户上线时再将消息进行发送，这里先不进行实现

                    continue;
                }

                // 获取到和目标服务器的session,将消息转发过去
                //这里的toServer在客户端和服务端进行连接时就已经当作userId来使用
                toSession = Constant.USERID_SESSION.get(userInServer);

                if (toSession == null) {
                    clientInit.start(userInServer);
                    // 休眠500毫秒，等待连接建立
                    Thread.sleep(500);
                    // 在次获取连接
                    toSession = Constant.USERID_SESSION.get(userInServer);
                }

                MessageDto messageDto = new MessageDto();
                BeanUtils.copyProperties(messageParam, messageDto);
                messageDto.setItemEventType(messageDto.getEventType());
                messageDto.setEventType("transmit");
                messageDto.setClientSign(String.valueOf(clientSign));
                sendMessage(toSession, JSON.toJSONString(messageDto), MsgTypeEnum.TEXT.getType());
            } catch (Exception e) {
                log.error("消息发送给指定用户出错：", e);
            }
        }
    }


    /**
     * 向客户端发送消息
     *
     * @param session
     * @param msg
     */
    public void sendMessage(Session session, String msg,String msgType) {

        if (MsgTypeEnum.TEXT.getType().equals(msgType)) {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                log.error("发送消息异常", e);
            }
        } else if (MsgTypeEnum.BYTE.getType().equals(msgType)) {
            try {
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes, 0, bytes.length));
            } catch (Exception e) {
                log.error("发送消息异常", e);
            }
        }

    }

    /**
     * 向客户端发送消息
     *
     * @param session
     * @param result
     */
    public void sendMessage(Session session, Result result) {
        sendMessage(session, JSON.toJSONString(result),result.getMsgType());
    }

    /**
     * 关闭客户端链接
     *
     * @param userKey
     */
    public void close(String userKey) {
        try {
            Session toSession = Constant.USERID_SESSION.get(userKey);

            if (toSession != null) {
                toSession.close();
                return;
            }

            // 从分布式存储中获取用户所链接的服务器
            String userInServer = redisUtils.get(Constant.USER_SERVER_REGISTER_KEY + userKey);
            if (userInServer == null) {
                // todo 代表当前用户不在线，可以先将消息进行存储，等到用户上线时再将消息进行发送，这里先不进行实现

                return;
            }

            // 获取到和目标服务器的session,将消息转发过去
            //这里的toServer在客户端和服务端进行连接时就已经当作userId来使用
            toSession = Constant.USERID_SESSION.get(userInServer);

            if (toSession == null) {
                clientInit.start(userInServer);
                // 休眠500毫秒，等待连接建立
                Thread.sleep(500);
                // 在次获取连接
                toSession = Constant.USERID_SESSION.get(userInServer);
            }
            MessageDto messageDto = new MessageDto();
            messageDto.setEventType("close");
            messageDto.setMsg(userKey);
            sendMessage(toSession, JSON.toJSONString(messageDto), MsgTypeEnum.TEXT.getType());
        } catch (Exception e) {
            log.error("链接关闭异常：", e);
        }
    }


}
