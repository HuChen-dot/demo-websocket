package com.hu.spring.server.interceptor;

import com.hu.spring.common.pojo.UserInfo;
import com.hu.spring.common.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class WebSocketInterceptor  implements HandshakeInterceptor {


    @Autowired
    private TokenService tokenService;

    /**
     * 建立请求之前，可以用来做权限判断
     *
     * @param request    the current request
     * @param response   the current response
     * @param wsHandler  the target WebSocket handler
     * @param attributes the attributes from the HTTP handshake to associate with the WebSocket
     *                   session; the provided attributes are copied, the original map is not used.
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            // 模拟用户（通常利用JWT令牌解析用户信息）
            String token = servletServerHttpRequest.getServletRequest().getParameter("token");
            String sourceSystem = servletServerHttpRequest.getServletRequest().getParameter("sourceSystem");
            String clientSign = servletServerHttpRequest.getServletRequest().getParameter("clientSign");
            try {
                UserInfo user = tokenService.getUser(token, sourceSystem, clientSign);
                // 设置当前这个session的属性，后续我们在发送消息时，可以通过 session.getAttributes().get("clientUserInfo")可以取出 clientUserInfo参数
                attributes.put("userInfo", user);
                attributes.put("clientSign", clientSign);
                attributes.put("sourceSystem", sourceSystem);
            } catch (Exception e) {
                log.error("webSocket 认证失败 ", e);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 建立请求之后
     *
     * @param request   the current request
     * @param response  the current response
     * @param wsHandler the target WebSocket handler
     * @param exception an exception raised during the handshake, or {@code null} if none
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }
}
