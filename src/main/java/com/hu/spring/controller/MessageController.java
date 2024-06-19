package com.hu.spring.controller;


import com.hu.spring.common.pojo.MessageParam;
import com.hu.spring.common.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/msg")
public class MessageController {


    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public String send(@RequestBody MessageParam messageParam) {
        messageService.sendToUser(messageParam);
        return "成功";
    }
}
