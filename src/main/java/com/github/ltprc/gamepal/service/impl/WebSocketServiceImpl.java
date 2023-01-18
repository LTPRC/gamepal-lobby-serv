package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.BasicInfo;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MessageService messageService;

    @Override
    public void onOpen(Session session, String userCode) {
        userService.getSessionMap().put(userCode, session);
        logger.info("建立连接成功");
    }

    @Override
    public void onClose(String userCode) {
        userService.getSessionMap().remove(userCode);
        logger.info("断开连接成功");
    }

    @Override
    public void onMessage(String message) {
//      System.out.println("Received String (size:" + message.length() + ")");
//      System.out.println("Received String:" + message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        if (null == jsonObject || !jsonObject.containsKey("userCode")) {
            logger.error(ErrorUtil.ERROR_1008);
            return;
        }
        String userCode = jsonObject.getString("userCode");
        // Receive basicInfo and update
        if (jsonObject.containsKey("basicInfo")) {
            BasicInfo basicInfo = jsonObject.getObject("basicInfo", BasicInfo.class);
            playerService.getBasicInfoMap().put(userCode, basicInfo);
        }
        // Receive playerInfo and update
        if (jsonObject.containsKey("playerInfo")) {
            PlayerInfo playerInfo = jsonObject.getObject("playerInfo", PlayerInfo.class);
            playerService.getPlayerInfoMap().put(userCode, playerInfo);
        }
        // Reply automatically
        communicate(userCode);
    }

    @Override
    public void communicate(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        rst.put("userCode", userCode);
        // Update token automatically
        String token = userService.updateTokenByUserCode(userCode);
        rst.put("token", token);
        // Flush messages automatically
        Map<String, Queue<Message>> messageMap = messageService.getMessageMap();
        if (messageMap.containsKey(userCode) && !messageMap.get(userCode).isEmpty()) {
            JSONArray messages = new JSONArray();
            messages.addAll(messageMap.get(userCode));
            messageMap.get(userCode).clear();
            rst.put("messages", messages);
        }
        // Return all detected basicInfos
        Map<String, BasicInfo> basicInfoMap = playerService.getBasicInfoMap();
        JSONArray basicInfos = new JSONArray();
        basicInfoMap.entrySet().stream().forEach(entry -> {
            JSONObject obj = new JSONObject();
            obj.put(entry.getKey(), entry.getValue());
            basicInfos.add(obj);
        });
        rst.put("basicInfos", basicInfos);
        // Return all detected playerInfos
        Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();
        JSONArray playerInfos = new JSONArray();
        playerInfoMap.entrySet().stream().forEach(entry -> {
            JSONObject obj = new JSONObject();
            obj.put(entry.getKey(), entry.getValue());
            playerInfos.add(obj);
        });
        rst.put("playerInfos", playerInfos);
        // Communicate
        String content = JSONObject.toJSONString(rst);
        try {
            userService.getSessionByUserCode(userCode).getBasicRemote().sendText(content);
        } catch (IOException e) {
            logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + userCode);
        }
    }
}
