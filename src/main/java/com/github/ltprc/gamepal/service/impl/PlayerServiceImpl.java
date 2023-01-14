package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.model.lobby.BasicInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.Drop;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
    private Map<String, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
    private Map<String, BasicInfo> basicInfoMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> relationMap = new ConcurrentHashMap<>();
    private Map<String, Drop> dropMap = new ConcurrentSkipListMap<>(); // dropCode, drop

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

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
        int value = req.getInteger("value");
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<>());
        }
        relationMap.get(userCode).put(nextUserCode, value);
        messageService.sendMessage(userCode, generateRelationMessage(userCode, nextUserCode, true, value));
        if (!relationMap.containsKey(nextUserCode)) {
            relationMap.put(nextUserCode, new ConcurrentHashMap<>());
        }
        relationMap.get(nextUserCode).put(userCode, value);
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
        JSONArray relations = new JSONArray();
        if (relationMap.containsKey(userCode)) {
            relationMap.get(userCode).entrySet().stream().forEach(entry -> {
                JSONObject relation = new JSONObject();
                relation.put("userCode", entry.getKey());
                relation.put("relation", entry.getValue());
                relations.add(relation);
            });
        }
        rst.put("relations", relations);
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
        Drop drop = new Drop(itemNo, amount, sceneNo, new Coordinate(x, y));
        String dropCode = UUID.randomUUID().toString();
        if (dropMap.containsKey(dropCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1001));
        }
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
    public ResponseEntity setBasicInfo(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        BasicInfo basicInfo = req.getObject("basicInfo", BasicInfo.class);
        basicInfoMap.put(userCode, basicInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity getBasicInfo(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        BasicInfo basicInfo = basicInfoMap.get(userCode);
        rst.put("basicInfo", basicInfo);
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

    private Message generateRelationMessage(String userCode, String nextUserCode, boolean isFrom, int newRelation) {
        Message message = new Message();
        message.setType(0);
        if (isFrom) {
            message.setToUserCode(userCode);
            message.setContent("你将对" + basicInfoMap.get(nextUserCode).getFullName() + "的关系调整为" + newRelation);
        } else {
            message.setToUserCode(nextUserCode);
            message.setContent(basicInfoMap.get(nextUserCode).getFullName() + "将对你的关系调整为" + newRelation);
        }
        return message;
    }
}
