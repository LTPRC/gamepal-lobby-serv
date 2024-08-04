package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingDeque;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
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

    private static final Log logger = LogFactory.getLog(MessageServiceImpl.class);

    @Autowired
    private UserService userService;

    /**
     * Send one message to the specific receiver.
     * As long as voice message cannot be transmitted by websocket, this method must be used.
     * @param request
     * @return
     */
    @Override
    public ResponseEntity<String> sendMessage(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        Message msg = JSON.parseObject(String.valueOf(req), Message.class);
        int scope = msg.getScope();
        String fromUserCode = msg.getFromUserCode();
        GameWorld fromWorld = userService.getWorldByUserCode(fromUserCode);
        int success = 0;
        int failure = 0;
        if (GamePalConstants.SCOPE_GLOBAL == scope) {
            for (Entry<String, Session> entry : fromWorld.getSessionMap().entrySet()) {
                if (!entry.getKey().equals(fromUserCode)) {
                    String toUserCode = entry.getKey();
                    ResponseEntity responseEntity = sendMessage(toUserCode, msg);
                    if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                        success++;
                    } else {
                        failure++;
                    }
                }
            }
        } else if (scope == GamePalConstants.SCOPE_INDIVIDUAL) {
            String toUserCode = msg.getToUserCode();
            if (toUserCode.equals(fromUserCode) || userService.getWorldByUserCode(toUserCode) == fromWorld) {
                ResponseEntity responseEntity = sendMessage(toUserCode, msg);
                if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                    success++;
                } else {
                    failure++;
                }
            }
        }
        rst.put("success", success);
        rst.put("failure", failure);
        return ResponseEntity.ok().body(rst.toString());
    }

    /**
     * 发送消息
     *
     * @param userCode
     * @param message
     */
    public ResponseEntity<String> sendMessage(String userCode, Message message) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Session session = world.getSessionMap().get(userCode);
        if (null == session) {
            logger.warn(ErrorUtil.ERROR_1009 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1009));
        }
        // Human-only
        if (world.getPlayerInfoMap().get(userCode).getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN) {
            Map<String, Queue<Message>> messageMap = world.getMessageMap();
            if (!messageMap.containsKey(userCode)) {
                messageMap.put(userCode, new LinkedBlockingDeque<>());
            }
            messageMap.get(userCode).add(message);
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
