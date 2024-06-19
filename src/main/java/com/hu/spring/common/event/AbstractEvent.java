package com.hu.spring.common.event;


import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理抽象类
 */
public abstract class AbstractEvent implements EventInterface {

    public static final Map<String, EventInterface> STRATEGY_TABLE = new HashMap<>();

    @PostConstruct
    public void strategy() {
        String type = eventType();
        if (!StringUtils.isEmpty(type)) {
            STRATEGY_TABLE.put(type, this);
        }
    }


    public abstract String eventType();

}
