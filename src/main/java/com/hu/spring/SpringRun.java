package com.hu.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@ComponentScan(basePackages = {"com.hu.spring", "com.hu.config", "com.hu.util"})
@EnableWebSocket
public class SpringRun {

    public static void main(String[] args) {
        SpringApplication.run(SpringRun.class, args);
    }
}
