package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.SceneModel;
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
import java.util.*;

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
        communicate(userCode);
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
        // Return stored token
        String token = userService.getTokenByUserCode(userCode);
        rst.put("token", token);
        // Flush messages automatically
        Map<String, Queue<Message>> messageMap = messageService.getMessageMap();
        if (messageMap.containsKey(userCode) && !messageMap.get(userCode).isEmpty()) {
            JSONArray messages = new JSONArray();
            messages.addAll(messageMap.get(userCode));
            messageMap.get(userCode).clear();
            rst.put("messages", messages);
        }
        // Based on current playerInfo, detect playerInfos from 9 around scenes
        Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        Set<PlayerInfo> rankedSet = new TreeSet<>(new Comparator<PlayerInfo>() {
            @Override
            public int compare(PlayerInfo o1, PlayerInfo o2) {
                return o1.getUserCoordinate().getPosition().getY().compareTo(o2.getUserCoordinate().getPosition().getY());
            }
        });
        playerInfoMap.entrySet().stream().forEach(entry -> {
            rankedSet.add(entry.getValue());
        });
        JSONObject playerInfos = new JSONObject();
        JSONArray detectedUserCodes = new JSONArray();
        rankedSet.stream().forEach(info -> {
            playerInfos.put(info.getUserCode(), info);
            detectedUserCodes.add(info.getUserCode());
        });
        rst.put("playerInfos", playerInfos);
        rst.put("detectedUserCodes", detectedUserCodes);
        // Communicate
        String content = JSONObject.toJSONString(rst);
        if (null == userService.getSessionByUserCode(userCode)
                || null == userService.getSessionByUserCode(userCode).getBasicRemote()) {
            logger.warn(ErrorUtil.ERROR_1003 + "userCode: " + userCode);
            return;
        }
        try {
            userService.getSessionByUserCode(userCode).getBasicRemote().sendText(content);
        } catch (IOException e) {
            logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + userCode);
        }
    }

    private int getCoordinateRelation(SceneModel from, SceneModel to) {
        if (to.getCenter() == from.getNorthwest()) {
            return 0;
        } else if (to.getCenter() == from.getNorth()) {
            return 1;
        } else if (to.getCenter() == from.getNortheast()) {
            return 2;
        } else if (to.getCenter() == from.getWest()) {
            return 3;
        } else if (to.getCenter() == from.getCenter()) {
            return 4;
        } else if (to.getCenter() == from.getEast()) {
            return 5;
        } else if (to.getCenter() == from.getSouthwest()) {
            return 6;
        } else if (to.getCenter() == from.getSouth()) {
            return 7;
        } else if (to.getCenter() == from.getSoutheast()) {
            return 8;
        } else {
            return -1;
        }
    }
}
