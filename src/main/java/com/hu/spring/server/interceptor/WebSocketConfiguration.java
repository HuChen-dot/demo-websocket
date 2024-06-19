package com.hu.spring.server.interceptor;

import com.hu.spring.server.ServerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfiguration implements WebSocketConfigurer {


    @Bean
    public ServerService defaultWebSocketHandler() {
        return new ServerService();
    }

    @Bean
    public WebSocketInterceptor webSocketInterceptor() {
        return new WebSocketInterceptor();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 链接方式如下 ws://127.0.0.1:9085/globalWs?token=qwncjncwqqdnjncz.adqwascsvcgrgb.cbrtbfvb
        // 如果你设置了springboot的 contentPath 那就需要在:9085端口后面 加上contentPath 的值，在拼接上  globalWs?token=qwncjncwqqdnjncz.adqwascsvcgrgb.cbrtbfvb
        registry.addHandler(defaultWebSocketHandler(), "/globalWs")
                .addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");
    }

}
