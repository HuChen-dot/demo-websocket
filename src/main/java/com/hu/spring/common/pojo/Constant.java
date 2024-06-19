package com.hu.spring.common.pojo;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {

    /**
     * 服务端注册中心的key
     */
    public static final String SERVER_REGISTER_KEY = "server_register_key";

    /**
     * 用户和服务器绑定的key，可以用userId直接获取改用户和哪台服务器绑定
     */
    public static final String USER_SERVER_REGISTER_KEY = "user_server_register_key";

    /**
     * 服务端上报心跳key
     */
    public static final String SERVER_REPORT_HEARTBEAT_KEY = "server_report_heartbeat_key";


    /**
     * 绑定用户和客户端，一个用户可能多账号登录，所以存在多个客户端
     */
    public static final String USER_CLIENT_SIGN_KEY = "user_client_sign_key";

    /**
     * key: userId
     * value: Session
     */
    public static final Map<String, WebSocketSession> USERID_SESSION = new ConcurrentHashMap<>();

    /**
     * key: SessionId
     * value: userId
     */
    public static final Map<String, String> SESSION_USERID = new ConcurrentHashMap<>();
}
