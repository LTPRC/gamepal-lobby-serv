package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Integer TYPE_PRINTED = 0;
    private static final Integer TYPE_VOICE = 1;
    private static final Integer SCOPE_GLOBAL = 0;
    private static final Integer SCOPE_INDIVIDUAL = 1;
    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
    private Map<String, Queue<Message>> messageMap = new ConcurrentHashMap<>(); // userCode, message queue

    @Autowired
    private UserService userService;

    /**
     * 发送消息
     *
     * @param userCode
     * @param message
     */
    public ResponseEntity sendMessage(String userCode, Message message) {
        JSONObject rst = ContentUtil.generateRst();
        Session session = userService.getSessionByUserCode(userCode);
        if (null == session) {
            logger.warn(ErrorUtil.ERROR_1009 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(ErrorUtil.ERROR_1009);
        }
        if(!messageMap.containsKey(userCode)) {
            messageMap.put(userCode, new LinkedBlockingDeque<>());
        }
        messageMap.get(userCode).add(message);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public Map<String, Queue<Message>> getMessageMap() {
        return messageMap;
    }

    /**
     * Send one message to the specific receiver.
     * @param request
     * @return
     */
    @Override
    public ResponseEntity sendMessage(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        int type = req.getInteger("type");
        int scope = req.getInteger("scope");
        String fromUserCode = req.getString("fromUserCode");
        String toUserCode = req.getString("toUserCode");
        String content = req.getString("content");
        int success = 0;
        int failure = 0;
        if (scope == SCOPE_GLOBAL) {
            for (Entry<String, Session> entry : userService.getSessionMap().entrySet()) {
                String userCode = entry.getKey();
                ResponseEntity responseEntity = sendMessage(userCode, new Message(type, scope, fromUserCode, toUserCode, content));
                if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                    success++;
                } else {
                    failure++;
                }
            }
        } else if (scope == SCOPE_INDIVIDUAL) {
            ResponseEntity responseEntity = sendMessage(toUserCode, new Message(type, scope, fromUserCode, toUserCode, content));
            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                success++;
            } else {
                failure++;
            }
        }
        rst.put("success", success);
        rst.put("failure", failure);
        return ResponseEntity.ok().body(rst.toString());
    }
}
