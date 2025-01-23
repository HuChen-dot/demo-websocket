package com.hu.tomcat.client;


import com.hu.tomcat.common.pojo.Constant;
import com.hu.util.IpUtil;
import com.hu.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
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
        String ip = IpUtil.getIp();
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
        String ip = IpUtil.getIp();
        String host = ip + ":" + port;
        // 进行链接
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(new ClientService(server), new URI("ws://" + server + "/globalWs/cluster/" + ip + "/" + host));
            container.setDefaultMaxSessionIdleTimeout(5000L);
        } catch (Exception e) {
            log.error("客户端连接服务端错误，服务端：{}，错误内容：{}", server, e);
        }
    }
}
