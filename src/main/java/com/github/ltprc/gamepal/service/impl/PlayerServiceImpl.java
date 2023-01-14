package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.model.lobby.BasicInfo;
import com.github.ltprc.gamepal.model.map.Drop;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceImpl implements PlayerService {

    private Map<String, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
    private Map<String, BasicInfo> basicInfoMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> relationMap = new ConcurrentHashMap<>();
    private Map<String, Drop> dropMap = new ConcurrentHashMap<>(); // dropNo, drop

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
