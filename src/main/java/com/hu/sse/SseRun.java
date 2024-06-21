package com.hu.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hu.sse", "com.hu.config", "com.hu.util"})
@EnableWebSocket
public class SseRun {

    public static void main(String[] args) {
        SpringApplication.run(SseRun.class, args);
    }
}
