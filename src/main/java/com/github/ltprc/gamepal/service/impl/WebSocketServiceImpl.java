package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.lobby.BasicInfo;
import com.github.ltprc.gamepal.model.lobby.PlayerInfo;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.util.Map;

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
        messageService.communicate(userCode);
    }
}
