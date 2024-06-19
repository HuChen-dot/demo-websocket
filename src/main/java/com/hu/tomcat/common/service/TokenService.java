package com.hu.tomcat.common.service;

import com.hu.tomcat.common.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 解析token，获取用户信息
 */
@Service
@Slf4j
public class TokenService {

    /**
     * 根据token获取用户信息
     *
     * @param token token
     */
    public UserInfo getUser(String token, String sourceSystem, String clientSign) {

        // 如果是客户端的注册链接另行处理
        if ("cluster".equals(sourceSystem)) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(token);
            userInfo.setUserName(sourceSystem);
            userInfo.setUserKey(userInfo.getUserId());
            return userInfo;
        }


        // 通过token从redis中获取用户信息，我这里直接模拟一个
//       UserInfo userInfo =  redis.getString(token);

        UserInfo userInfo = new UserInfo();

        userInfo.setUserId(token);
        userInfo.setUserName("admin");
        userInfo.setUserKey(userInfo.getUserId() + sourceSystem + "_" + clientSign);
        return userInfo;
    }
}
