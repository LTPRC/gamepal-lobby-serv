package com.github.ltprc.gamepal.controller;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.github.ltprc.gamepal.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.UserService;

@Component
@ServerEndpoint("/websocket/v1/{userCode}")
public class WebSocketController {

    private static WebSocketService webSocketService;

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        WebSocketController.webSocketService = webSocketService;
    }

    /**
     * 建立连接调用的方法
     * @param session
     * @param userCode
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userCode") String userCode) {
        webSocketService.onOpen(session, userCode);;
    }

    /**
     * 关闭链接调用接口
     * @param userCode
     */
    @OnClose
    public void onClose(@PathParam("userCode") String userCode) {
        webSocketService.onClose(userCode);
    }

    /**
     * 接收消息
     * @param message
     */
    @OnMessage
    public void onMessage(@NonNull String message) {
        webSocketService.onMessage(message);
    }
}