package com.github.ltprc.gamepal.controller;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.UserService;

@Component
@ServerEndpoint("/websocket/v1/{userCode}")
public class WebSocketController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    /**
     * 建立连接调用的方法
     * @param session
     * @param userCode
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userCode") String userCode) {
        userService.onOpen(session, userCode);;
    }

    /**
     * 关闭链接调用接口
     * @param userCode
     */
    @OnClose
    public void onClose(@PathParam("userCode") String userCode) {
        userService.onClose(userCode);
    }

    /**
     * 接收消息
     * @param message
     */
    @OnMessage
    public void onMessage(@NonNull String message) {
        messageService.onMessage(message);
    }
}