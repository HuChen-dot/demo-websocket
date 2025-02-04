package com.hu.tomcat.server;

import com.hu.tomcat.common.pojo.Constant;
import com.hu.util.IpUtil;
import com.hu.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务端初始化
 */
@Slf4j
@Configuration
public class ServerInit {


    @Value("${server.port}")
    public String port;

    @Autowired
    private RedisUtils redisUtils;

    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    /**
     * 初始化Bean，它会自动注册使用了 @ServerEndpoint 注解声明的 WebSocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 随着项目启动而启动
     */
    @PostConstruct
    public void init() {
        // 将当前服务端注册到注册中心
        String ip = IpUtil.getIp();
        String host = ip + ":" + port;
        redisUtils.setForSet(Constant.SERVER_REGISTER_KEY, Arrays.asList(host));

        // 启动心跳包定时任务，定时上报自己存活的信息,3秒钟上报一次信息
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            redisUtils.set(Constant.SERVER_REPORT_HEARTBEAT_KEY + host, String.valueOf(System.currentTimeMillis()));
            redisUtils.setForSet(Constant.SERVER_REGISTER_KEY, Arrays.asList(host));
        }, 0L, 3, TimeUnit.SECONDS);
    }


    /**
     * 随着项目关闭而关闭
     */
    @PreDestroy
    public void stop() {
        // 关闭服务
        String ip = IpUtil.getIp();
        String host = ip + ":" + port;
        redisUtils.delForSet(Constant.SERVER_REGISTER_KEY,host);
        log.info("websocket服务端关闭");
    }
}
