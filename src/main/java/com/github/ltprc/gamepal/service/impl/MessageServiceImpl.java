package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;

import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.MessageConstants;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.apache.commons.lang3.StringUtils;
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

    private static final Log logger = LogFactory.getLog(MessageServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

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
        if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
            sendMessage(fromUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope, fromUserCode,
                    fromUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
        }
        GameWorld world = userService.getWorldByUserCode(fromUserCode);
        Block player = world.getCreatureMap().get(fromUserCode);
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        if (MessageConstants.SCOPE_GLOBAL == scope) {
            world.getPlayerInfoMap().entrySet().stream()
                    .filter(entry -> entry.getValue().getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN)
//                    .filter(entry -> playerService.validateActiveness(world, entry.getKey()))
                    .forEach(entry -> {
                if (!entry.getKey().equals(fromUserCode)) {
                    String toUserCode = entry.getKey();
                    if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
                        sendMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
                                fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
                    }
                    sendMessage(toUserCode, msg);
                }
            });
        } else if (MessageConstants.SCOPE_TEAMMATE == scope) {
            world.getPlayerInfoMap().entrySet().stream()
                    .filter(entry -> entry.getValue().getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN)
//                    .filter(entry -> playerService.validateActiveness(world, entry.getKey()))
                    .filter(entry -> StringUtils.equals(playerService.findTopBossId(entry.getKey()),
                            playerService.findTopBossId(fromUserCode)))
                    .forEach(entry -> {
                if (!entry.getKey().equals(fromUserCode)) {
                    String toUserCode = entry.getKey();
                    if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
                        sendMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
                                fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
                    }
                    sendMessage(toUserCode, msg);
                }
            });
        } else if (scope == MessageConstants.SCOPE_INDIVIDUAL) {
            String toUserCode = msg.getToUserCode();
            if (toUserCode.equals(fromUserCode) || userService.getWorldByUserCode(toUserCode) == world) {
                if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
                    sendMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
                            fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
                }
                sendMessage(toUserCode, msg);
            }
        } else if (scope == MessageConstants.SCOPE_SELF) {
            sendMessage(fromUserCode, msg);
        } else if (scope == MessageConstants.SCOPE_NEARBY) {
            world.getPlayerInfoMap().entrySet().stream()
                    .filter(entry -> entry.getValue().getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN)
                    .filter(entry -> {
                        BigDecimal distance = BlockUtil.calculateDistance(region, player.getWorldCoordinate(),
                                world.getCreatureMap().get(entry.getKey()).getWorldCoordinate());
                        return null != distance
                                && distance.compareTo(CreatureConstants.DEFAULT_INDISTINCT_HEARING_RADIUS) <= 0;
                    })
                    .forEach(entry -> {
                        if (!entry.getKey().equals(fromUserCode)) {
                            String toUserCode = entry.getKey();
                            if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
                                sendMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
                                        fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
                            }
                            sendMessage(toUserCode, msg);
                        }
                    });
            sendMessage(fromUserCode, msg);
        }
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
        // These messages are shown back to the sender (human-only) 24/08/09
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
