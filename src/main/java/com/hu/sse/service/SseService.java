package com.hu.sse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SseService {

    /**
     * 服务端和客户端连接维持的时间，单位 秒
     * 到达超时时间之后，服务端就会与前端断开连接，即使后续还有推流内容前端也无法接收到。
     * 所以此超时时间需要根据实际情况合理设置：
     * 如果设置的太短，消息无法完整给到前端；
     * 如果设置的太长又没有正确的主动关闭长链接，会造成服务端资源浪费
     */
    private static Integer DEFAULT_TIME_OUT = 5 * 60;

    /**
     * 用来存储和客户端的连接。
     */
    private static final Map<String, SseEmitter> subscribeMap = new ConcurrentHashMap<>();

    /**
     * 添加订阅
     *
     * @param requestId 请求ID
     * @return
     */
    public static SseEmitter addSub(String requestId) {
        return addSub(requestId, DEFAULT_TIME_OUT);
    }


    /**
     * 添加订阅
     *
     * @param requestId 请求ID
     * @param timeout   服务端和客户端连接超时时间，当超过这个时间连接将失效
     * @return
     */
    public static SseEmitter addSub(String requestId, Integer timeout) {
        if (StringUtils.isEmpty(requestId)) {
            return null;
        }
        SseEmitter emitter = subscribeMap.get(requestId);

        if (null == emitter) {
            emitter = new SseEmitter(timeout * 1000L);

            emitter.onTimeout(() -> {
                // 当注册连接超时回调，超时后触发
                closeSub(requestId);
            });

            emitter.onCompletion(() -> {
                // 当手动调用 emitter.complete() 时触发。代表已经不需要这条连接
                closeSub(requestId);
            });
            subscribeMap.put(requestId, emitter);
        }
        return emitter;
    }

    /**
     * 给客户端发送消息
     * @param requestId 请求ID
     * @param eventName  事件名称，客户端可以根据这个事件名称，做不同的动作
     * @param msg
     */
    public static void pubMsg(String requestId,String eventName, Object msg) {
        SseEmitter emitter = subscribeMap.get(requestId);
        if (null != emitter) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(msg, MediaType.TEXT_EVENT_STREAM));
            } catch (Exception e) {
                log.error("消息推送失败，消息内容：{}，异常信息：", msg, e);
            }
        }
    }

    // 关闭订阅
    public static void closeSub(String requestId) {
        SseEmitter emitter = subscribeMap.get(requestId);
        if (null != emitter) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("关闭连接失败：",e);
            } finally {
                subscribeMap.remove(requestId);
            }
        }
    }


}
