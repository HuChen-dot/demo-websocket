package com.hu.spring.client;


import com.hu.spring.common.pojo.Constant;
import com.hu.util.LocalIpUtils;
import com.hu.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * 客户端初始化链接其他服务端
 */
@Component
@Slf4j
public class ClientInit {

    @Autowired
    private RedisUtils redisUtils;

    @Value("${server.port}")
    private String port;

    @PostConstruct
    public void init() throws Exception {
        // 从注册中心获取其他服务器的信息进行链接
        Set<String> forSetAll = redisUtils.getForSetAll(Constant.SERVER_REGISTER_KEY);

        if (CollectionUtils.isEmpty(forSetAll)) {
            return;
        }
        // 当前服务器url
        String ip = LocalIpUtils.getIntranetIP();
        String host = ip + ":" + port;
        for (String server : forSetAll) {
            if (server.equals(host)) {
                continue;
            }
            // 获取该服务的心跳时间
            String time = redisUtils.get(Constant.SERVER_REPORT_HEARTBEAT_KEY + server);
            if (time == null || System.currentTimeMillis() - Long.parseLong(time) > 8000) {
                // 代表该服务器已经超过3秒钟未上传心跳，默认该服务已死,跳过链接并删除该服务
                redisUtils.delForSet(Constant.SERVER_REGISTER_KEY, server);
                continue;
            }

            start(server);
        }
    }

    public void start(String server){
        String ip = LocalIpUtils.getIntranetIP();
        String host = ip + ":" + port;
        // 进行链接
        try {
            WebSocketClient client = new StandardWebSocketClient();
            WebSocketHandler handler = new ClientService(server);
            WebSocketConnectionManager manager = new WebSocketConnectionManager(client, handler, "ws://" + server + "/globalWs?sourceSystem=cluster&clientSign=" + ip + "&token=" + host);
            manager.start();
        } catch (Exception e) {
            log.error("客户端连接服务端错误，服务端：{}，错误内容：{}", server, e);
        }
    }
}
