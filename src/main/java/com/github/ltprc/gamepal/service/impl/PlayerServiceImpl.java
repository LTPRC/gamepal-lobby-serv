package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.map.world.PlayerInfo;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Integer RELATION_INIT = 0;
    private static final Integer RELATION_MIN = -100;
    private static final Integer RELATION_MAX = 100;
    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
    private Map<String, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> relationMap = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Override
    public ResponseEntity setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!playerInfoMap.containsKey(nextUserCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<>());
        }
        if (!relationMap.containsKey(nextUserCode)) {
            relationMap.put(nextUserCode, new ConcurrentHashMap<>());
        }
        if (!relationMap.get(userCode).containsKey(nextUserCode)) {
            relationMap.get(userCode).put(nextUserCode, RELATION_INIT);
        }
        if (!relationMap.get(nextUserCode).containsKey(userCode)) {
            relationMap.get(nextUserCode).put(userCode, RELATION_INIT);
        }
        if (!isAbsolute) {
            newRelation += relationMap.get(userCode).get(nextUserCode);
        }
        newRelation = Math.min(RELATION_MAX, Math.max(RELATION_MIN, newRelation));
        relationMap.get(userCode).put(nextUserCode, newRelation);
        relationMap.get(nextUserCode).put(userCode, newRelation);
        messageService.sendMessage(userCode, generateRelationMessage(userCode, nextUserCode, true, newRelation));
        rst.put("userCode", userCode);
        rst.put("nextUserCode", nextUserCode);
        rst.put("relation", newRelation);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity updateplayerinfobyentities(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        if (!playerInfoMap.containsKey(userCode)) {
            playerInfoMap.put(userCode, new PlayerInfo());
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        String firstName = req.getString("firstName");
        if (StringUtils.hasText(firstName)) {
            playerInfo.setFirstName(firstName);
        }
        String lastName = req.getString("lastName");
        if (StringUtils.hasText(lastName)) {
            playerInfo.setLastName(lastName);
        }
        String nickname = req.getString("nickname");
        if (StringUtils.hasText(nickname)) {
            playerInfo.setNickname(nickname);
        }
        String nameColor = req.getString("nameColor");
        if (StringUtils.hasText(nameColor)) {
            playerInfo.setNameColor(nameColor);
        }
        String creature = req.getString("creature");
        if (StringUtils.hasText(creature)) {
            playerInfo.setCreature(creature);
        }
        String gender = req.getString("gender");
        if (StringUtils.hasText(gender)) {
            playerInfo.setGender(gender);
        }
        String skinColor = req.getString("skinColor");
        if (StringUtils.hasText(skinColor)) {
            playerInfo.setSkinColor(skinColor);
        }
        String hairstyle = req.getString("hairstyle");
        if (StringUtils.hasText(hairstyle)) {
            playerInfo.setHairstyle(hairstyle);
        }
        String hairColor = req.getString("hairColor");
        if (StringUtils.hasText(hairColor)) {
            playerInfo.setHairColor(hairColor);
        }
        String eyes = req.getString("eyes");
        if (StringUtils.hasText(eyes)) {
            playerInfo.setEyes(eyes);
        }
        String avatar = req.getString("avatar");
        if (StringUtils.hasText(avatar)) {
            playerInfo.setAvatar(avatar);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity getPlayerInfo(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        rst.put("playerInfo", playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public Map<String, PlayerInfo> getPlayerInfoMap() {
        return playerInfoMap;
    }

    private Message generateRelationMessage(String userCode, String nextUserCode, boolean isFrom, int newRelation) {
        Message message = new Message();
        message.setType(0);
        if (isFrom) {
            message.setToUserCode(userCode);
            message.setContent("你将对" + playerInfoMap.get(nextUserCode).getNickname() + "的关系调整为" + newRelation);
        } else {
            message.setToUserCode(nextUserCode);
            message.setContent(playerInfoMap.get(nextUserCode).getNickname() + "将对你的关系调整为" + newRelation);
        }
        return message;
    }

    @Override
    public Map<String, Integer> getRelationMapByUserCode(String userCode) {
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<String, Integer>());
        }
        return relationMap.get(userCode);
    }
}
