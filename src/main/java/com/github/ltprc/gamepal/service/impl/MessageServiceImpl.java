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
//        try {
//            session.getBasicRemote().sendText(message);
//        } catch (IOException e) {
//            logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + userCode);
//            return ResponseEntity.badRequest().body(ErrorUtil.ERROR_1010);
//        }
        if(!messageMap.containsKey(userCode)) {
            messageMap.put(userCode, new LinkedBlockingDeque<>());
        }
        messageMap.get(userCode).add(message);
        return ResponseEntity.ok().body(rst.toString());
    }

    /**
     * 集体发送消息
     * @param message
     */
    @Override
    public ResponseEntity sendMessageToAll(Message message) {
        JSONObject rst = ContentUtil.generateRst();
        for (Entry<String, Session> entry : userService.getSessionMap().entrySet()) {
//            try {
//                entry.getValue().getBasicRemote().sendText(message);
//            } catch (IOException e) {
//                logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + entry.getKey());
//                return ResponseEntity.badRequest().body(ErrorUtil.ERROR_1010);
//            }
            String userCode = entry.getKey();
            sendMessage(userCode, message);
        }
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
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String fromUserCode = req.getString("fromUserCode");
        String toUserCode = req.getString("toUserCode");
        int type = req.getInteger("fromUserCode");
        String content = req.getString("content");
        return sendMessage(toUserCode, new Message(fromUserCode, toUserCode, type, content));
    }
}
