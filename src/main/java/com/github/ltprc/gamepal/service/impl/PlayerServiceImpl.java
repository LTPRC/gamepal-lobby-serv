package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.lobby.Drop;
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
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Integer RELATION_INIT = 0;
    private static final Integer RELATION_MIN = -100;
    private static final Integer RELATION_MAX = 100;
    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
    private Map<String, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> relationMap = new ConcurrentHashMap<>();
    private Map<String, Drop> dropMap = new ConcurrentSkipListMap<>(); // userCode, drop

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Override
    public ResponseEntity setRelation(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        String nextUserCode = req.getString("nextUserCode");
        boolean isAbsolute = req.getBoolean("isAbsolute");
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!playerInfoMap.containsKey(nextUserCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        int newRelation = req.getInteger("newRelation");
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
    public ResponseEntity getRelation(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        String nextUserCode = req.getString("nextUserCode");
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
        rst.put("userCode", userCode);
        rst.put("nextUserCode", nextUserCode);
        rst.put("relation", relationMap.get(userCode).get(nextUserCode));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity setDrop(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String itemNo = req.getString("itemNo");
        Integer amount = req.getInteger("amount");
        Integer sceneNo = req.getInteger("sceneNo");
        BigDecimal x = req.getBigDecimal("x");
        BigDecimal y = req.getBigDecimal("y");
        Drop drop = new Drop(itemNo, amount);
        String dropCode = UUID.randomUUID().toString();
        if (dropMap.containsKey(dropCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1001));
        }
        drop.setUserCode(dropCode);
        drop.setSceneNo(sceneNo);
        drop.setPosition(new Coordinate(x, y));
        dropMap.put(dropCode, drop);
        rst.put("dropCode", dropCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity getDrop(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
//        String userCode = req.getString("userCode");
        String dropCode = req.getString("dropCode");
        if (!dropMap.containsKey(dropCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1012));
        }
        Drop drop = dropMap.get(dropCode);
        rst.put("drop", drop);
        // Get the item from the drop immediately
//        int amount = playerInfoMap.get(userCode).getItems().get(drop.getItemNo());
//        if (drop.getAmount() > 0 && Integer.MAX_VALUE - amount < drop.getAmount()) {
//            amount = Integer.MAX_VALUE;
//            logger.warn(ErrorUtil.ERROR_1013);
//        } else {
//            amount += drop.getAmount();
//        }
//        playerInfoMap.get(userCode).getItems().put(drop.getItemNo(), amount);
        dropMap.remove(dropCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity setPlayerInfo(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        PlayerInfo playerInfo = req.getObject("playerInfo", PlayerInfo.class);
        playerInfoMap.put(userCode, playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity setPlayerInfoByEntities(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            logger.error(ErrorUtil.ERROR_1002);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        if (null == req || !req.containsKey("userCode")) {
            logger.error(ErrorUtil.ERROR_1008);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1008));
        }
        String userCode = req.getString("userCode");
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
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

    @Override
    public Map<String, Drop> getDropMap() {
        return dropMap;
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
}
