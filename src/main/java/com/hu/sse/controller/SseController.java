package com.hu.sse.controller;

import com.hu.sse.pojo.Body;
import com.hu.sse.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SseController {



    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter test1(Body body) {
        // 添加订阅,建立sse链接
        SseEmitter emitter = SseService.addSub(body.getRequestId());

        // 开启新线程处理异步任务
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    // 发送消息
                    SseService.pubMsg(body.getRequestId(),"下载事件",body.getRequestId() + " - 下载操作已完成，请前往下载中心查看 " + i);
                    Thread.sleep(3 * 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                // 消息发送完关闭订阅
                SseService.closeSub(body.getRequestId());
            }
        }).start();
        return emitter;
    }


    @RequestMapping(value = "/test2", method = RequestMethod.POST, produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter test2(@RequestBody Body body) {

        // 添加订阅,建立sse链接
        SseEmitter emitter = SseService.addSub(body.getRequestId());

        // 开启新线程处理异步任务
        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    // 发送消息
                    SseService.pubMsg(body.getRequestId(),"下载事件",body.getRequestId() + " - hmg come " + i);
                    Thread.sleep(3 * 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                // 消息发送完关闭订阅
                SseService.closeSub(body.getRequestId());
            }
        }).start();


        return emitter;
    }


}
