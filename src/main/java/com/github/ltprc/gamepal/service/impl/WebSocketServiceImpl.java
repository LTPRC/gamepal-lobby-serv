package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.Drop;
import com.github.ltprc.gamepal.model.lobby.Event;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.model.map.SceneCoordinate;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.PlayerUtil;
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

        // Return maps of playerInfos, drops, and events based on detected scenes
        Set<SceneCoordinate> rankedSet = new TreeSet<>(new Comparator<SceneCoordinate>() {
            @Override
            public int compare(SceneCoordinate o1, SceneCoordinate o2) {
                return o1.getPosition().getY().compareTo(o2.getPosition().getY());
            }
        });
        Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);

        JSONObject playerInfos = new JSONObject();
        playerInfoMap.entrySet().stream()
                .filter(entry -> -1 != PlayerUtil.getCoordinateRelation(playerInfo.getScenes(),
                        entry.getValue().getSceneNo())).forEach(entry -> {
                    playerInfos.put(entry.getKey(), entry.getValue());
                    rankedSet.add(entry.getValue());
                });
        rst.put("playerInfos", playerInfos);

        JSONObject drops = new JSONObject();
        Map<String, Drop> dropMap = playerService.getDropMap();
        dropMap.entrySet().stream()
                .filter(entry -> -1 != PlayerUtil.getCoordinateRelation(playerInfo.getScenes(),
                        entry.getValue().getSceneNo())).forEach(entry -> {
                    drops.put(entry.getKey(), entry.getValue());
                    rankedSet.add(entry.getValue());
                });
        rst.put("drops", drops);

        JSONObject events = new JSONObject();
        Map<String, Event> eventMap = playerService.getEventMap();
        eventMap.entrySet().stream()
                .filter(entry -> -1 != PlayerUtil.getCoordinateRelation(playerInfo.getScenes(),
                        entry.getValue().getSceneNo())).forEach(entry -> {
                    events.put(entry.getKey(), entry.getValue());
                    rankedSet.add(entry.getValue());
                });
        rst.put("events", events);

        JSONArray detectedObjects = new JSONArray();
        rankedSet.stream().forEach(info -> {
            JSONObject detectedObject = new JSONObject();
            detectedObject.put("userCode", userCode);
            detectedObject.put("type", info.detectionType);
            detectedObjects.add(detectedObject);
        });
        rst.put("detectedObjects", detectedObjects);

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
}
