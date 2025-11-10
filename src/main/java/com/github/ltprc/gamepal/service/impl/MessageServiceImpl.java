package com.github.ltprc.gamepal.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import javax.servlet.http.HttpServletRequest;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.MessageConstants;
import com.github.ltprc.gamepal.manager.CommandManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.QwenResponse;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WebService;
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
    private static final Random random = new Random();

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WebService webService;

    @Autowired
    private CommandManager commandManager;

    @Autowired
    private SceneManager sceneManager;

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
        if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
            collectMessage(msg.getFromUserCode(), new Message(MessageConstants.MESSAGE_TYPE_PRINTED, msg.getScope(),
                    msg.getFromUserCode(), msg.getToUserCode(), MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
        }
//        GameWorld world = userService.getWorldByUserCode(fromUserCode);
//        Block player = world.getCreatureMap().get(fromUserCode);
//        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
//        if (MessageConstants.SCOPE_GLOBAL == scope) {
//            world.getPlayerInfoMap().entrySet().stream()
////                    .filter(entry -> entry.getValue().getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN)
////                    .filter(entry -> playerService.validateActiveness(world, entry.getKey()))
//                    .forEach(entry -> {
//                if (!entry.getKey().equals(fromUserCode)) {
//                    String toUserCode = entry.getKey();
//                    if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
//                        receiveMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
//                                fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
//                    }
//                    receiveMessage(toUserCode, msg);
//                }
//            });
//        } else if (MessageConstants.SCOPE_TEAMMATE == scope) {
//            world.getPlayerInfoMap().entrySet().stream()
////                    .filter(entry -> entry.getValue().getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN)
////                    .filter(entry -> playerService.validateActiveness(world, entry.getKey()))
//                    .filter(entry -> StringUtils.equals(playerService.findTopBossId(entry.getKey()),
//                            playerService.findTopBossId(fromUserCode)))
//                    .forEach(entry -> {
//                if (!entry.getKey().equals(fromUserCode)) {
//                    String toUserCode = entry.getKey();
//                    if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
//                        receiveMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
//                                fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
//                    }
//                    receiveMessage(toUserCode, msg);
//                }
//            });
//        } else if (scope == MessageConstants.SCOPE_INDIVIDUAL) {
//            String toUserCode = msg.getToUserCode();
//            if (toUserCode.equals(fromUserCode) || userService.getWorldByUserCode(toUserCode) == world) {
//                if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
//                    receiveMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
//                            fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
//                }
//                receiveMessage(toUserCode, msg);
//            }
//        } else if (scope == MessageConstants.SCOPE_SELF) {
//            receiveMessage(fromUserCode, msg);
//        } else if (scope == MessageConstants.SCOPE_NEARBY) {
//            world.getPlayerInfoMap().entrySet().stream()
////                    .filter(entry -> entry.getValue().getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN)
//                    .filter(entry -> {
//                        BigDecimal distance = BlockUtil.calculateDistance(region, player.getWorldCoordinate(),
//                                world.getCreatureMap().get(entry.getKey()).getWorldCoordinate());
//                        return null != distance
//                                && distance.compareTo(CreatureConstants.DEFAULT_INDISTINCT_HEARING_RADIUS) <= 0;
//                    })
//                    .forEach(entry -> {
//                        if (!entry.getKey().equals(fromUserCode)) {
//                            String toUserCode = entry.getKey();
//                            if (msg.getType() == MessageConstants.MESSAGE_TYPE_VOICE) {
//                                receiveMessage(toUserCode, new Message(MessageConstants.MESSAGE_TYPE_PRINTED, scope,
//                                        fromUserCode, toUserCode, MessageConstants.MESSAGE_PRINTED_CONTENT_VOICE));
//                            }
//                            receiveMessage(toUserCode, msg);
//                        }
//                    });
//            receiveMessage(fromUserCode, msg);
//        }
        collectMessage(msg.getFromUserCode(), msg);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> collectMessage(String userCode, Message msg) {
        JSONObject rst = ContentUtil.generateRst();
        if (null != msg.getContent() && msg.getContent().indexOf(MessageConstants.COMMAND_PREFIX) == 0) {
            // Command detected
            String commandContent = StringUtils.trim(msg.getContent().substring(1));
            return commandManager.useCommand(userCode, commandContent);
        }
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Block player = world.getCreatureMap().get(userCode);
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        switch (msg.getScope()) {
            case MessageConstants.SCOPE_GLOBAL:
                world.getOnlineMap().keySet().stream()
                        .filter(id -> !StringUtils.equals(id, userCode))
                        .forEach(id -> saveMessage(id, msg));
                break;
            case MessageConstants.SCOPE_TEAMMATE:
                world.getOnlineMap().keySet().stream()
                        .filter(id -> !StringUtils.equals(id, userCode))
                        .filter(id -> StringUtils.equals(playerService.findTopBossId(id),
                                playerService.findTopBossId(msg.getFromUserCode())))
                        .forEach(id -> saveMessage(id, msg));
                break;
            case MessageConstants.SCOPE_INDIVIDUAL:
                saveMessage(msg.getToUserCode(), msg);
                break;
            case MessageConstants.SCOPE_NEARBY:
                world.getOnlineMap().keySet().stream()
                        .filter(id -> !StringUtils.equals(id, userCode))
                        .filter(id -> {
                            BigDecimal distance = BlockUtil.calculateDistance(region, player.getWorldCoordinate(),
                                    world.getCreatureMap().get(id).getWorldCoordinate());
                            return null != distance
                                    && distance.compareTo(CreatureConstants.DEFAULT_INDISTINCT_HEARING_RADIUS) <= 0;
                        })
                        .forEach(id -> saveMessage(id, msg));
                break;
            case MessageConstants.SCOPE_SELF:
            default:
                break;
        }
        saveMessage(msg.getFromUserCode(), msg);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public void saveMessage(String userCode, Message msg) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1019 + "userCode: " + userCode);
            return;
        }
        if (GamePalConstants.PLAYER_TYPE_HUMAN == world.getPlayerInfoMap().get(userCode).getPlayerType()) {
            Map<String, Queue<Message>> messageMap = world.getMessageMap();
            messageMap.putIfAbsent(userCode, new LinkedBlockingDeque<>());
            messageMap.get(userCode).add(msg);
            Map<String, Block> creatureMap = world.getCreatureMap();
            if (!creatureMap.containsKey(userCode)) {
                logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
                return;
            }
            if (MessageConstants.SCOPE_SELF != msg.getScope()) {
                WorldCoordinate senderWc = creatureMap.get(msg.getFromUserCode()).getWorldCoordinate();
                sceneManager.addTextDisplayBlock(world, BlockUtil.locateCoordinateWithDirectionAndDistance(
                        world.getRegionMap().get(senderWc.getRegionNo()), senderWc,
                        BigDecimal.valueOf(random.nextDouble() * 360),
                        BlockConstants.TEXT_DISPLAY_SHIFT_DISTANCE),
                        BlockConstants.BLOCK_CODE_TEXT_DISPLAY, msg.getContent());
            }
        } else if (GamePalConstants.PLAYER_TYPE_NPC == world.getPlayerInfoMap().get(userCode).getPlayerType()
                && CreatureConstants.CREATURE_TYPE_HUMAN == world.getPlayerInfoMap().get(userCode).getCreatureType()
                && !StringUtils.equals(userCode, msg.getFromUserCode())) {
            npcReactMessage(userCode, msg);
        }
    }

    private void npcReactMessage(String userCode, Message msg) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1019 + "userCode: " + userCode);
            return;
        }
        StringBuilder messageSb = new StringBuilder();
        messageSb.append("回答" + world.getPlayerInfoMap().get(msg.getFromUserCode()).getNickname()
                + "向你" + world.getPlayerInfoMap().get(userCode).getNickname() + "发来的消息。");
        messageSb.append("消息范围:");
        switch (msg.getType()) {
            case MessageConstants.SCOPE_GLOBAL:
                messageSb.append("全局广播 ");
                break;
            case MessageConstants.SCOPE_TEAMMATE:
                messageSb.append("团队内部 ");
                break;
            case MessageConstants.SCOPE_INDIVIDUAL:
                messageSb.append("一对一 ");
                break;
            case MessageConstants.SCOPE_SELF:
                messageSb.append("自己对自己 ");
                break;
            case MessageConstants.SCOPE_NEARBY:
                messageSb.append("附近的人 ");
                break;
        }
        messageSb.append("消息内容:\\\"")
                .append(msg.getContent())
                .append("\\\" ");
        messageSb.append("你的个人信息:")
                .append(playerService.toReadableString(world.getCreatureMap().get(userCode),
                        world.getPlayerInfoMap().get(userCode)))
                .append(" ");
        messageSb.append("对方个人信息:")
                .append(playerService.toReadableString(world.getCreatureMap().get(msg.getFromUserCode()),
                        world.getPlayerInfoMap().get(msg.getFromUserCode())))
                .append(" ");
        messageSb.append("你们之间的友好度(整数取值范围" + CreatureConstants.RELATION_MIN)
                .append("至" + CreatureConstants.RELATION_MAX + "):")
                .append(world.getRelationMap().get(userCode).get(msg.getFromUserCode()))
                .append(" ");
        messageSb.append("以JSON数组的结构返回结果。");
        messageSb.append("第一项代表回答以后你们之间的友好度(整数取值范围" + CreatureConstants.RELATION_MIN + "至"
                        + CreatureConstants.RELATION_MAX + ")");
        messageSb.append("第二项代表回答回答内容。 ");
        webService.callQwenApiAsync("qwen-plus", messageSb.toString())
                .thenAccept(qwenResponse -> {
                    Optional.ofNullable(qwenResponse)
                            .map(QwenResponse::getOutput)
                            .map(QwenResponse.Output::getText)
                            .filter(text -> !text.trim().isEmpty())
                            .map(text -> {
                                try {
                                    return JSON.parseArray(text).toJavaList(String.class);
                                } catch (Exception e) {
                                    logger.error(ErrorUtil.ERROR_1046 + "userCode: " + userCode);
                                    return null;
                                }
                            })
                            .filter(list -> list.size() >= 2)
                            .ifPresent(qwenResponseList -> {
                                try {
                                    int newRelation = Integer.parseInt(qwenResponseList.get(0));
                                    playerService.setRelation(userCode, msg.getFromUserCode(), newRelation, true, true);
                                } catch (NumberFormatException e) {
                                    logger.error(ErrorUtil.ERROR_1046 + "userCode: " + userCode);
                                    return;
                                }
                                String content = qwenResponseList.get(1);
                                Message returnedMessage = new Message();
                                returnedMessage.setType(MessageConstants.MESSAGE_TYPE_PRINTED);
                                returnedMessage.setScope(MessageConstants.SCOPE_INDIVIDUAL);
                                returnedMessage.setFromUserCode(userCode);
                                returnedMessage.setToUserCode(msg.getFromUserCode());
                                returnedMessage.setContent(content);
                                collectMessage(msg.getFromUserCode(), returnedMessage);
                            });
                    if (null != qwenResponse && null != qwenResponse.getOutput()) {
                        List<String> qwenResponseList
                                = JSON.parseArray(qwenResponse.getOutput().getText()).toJavaList(String.class);
                        int newRelation = Integer.parseInt(qwenResponseList.get(0));
                        playerService.setRelation(userCode, msg.getFromUserCode(), newRelation, true, true);
                        String content = qwenResponseList.get(1);
                        Message returnedMessage = new Message();
                        returnedMessage.setType(MessageConstants.MESSAGE_TYPE_PRINTED);
                        returnedMessage.setScope(MessageConstants.SCOPE_INDIVIDUAL);
                        returnedMessage.setFromUserCode(userCode);
                        returnedMessage.setToUserCode(msg.getFromUserCode());
                        returnedMessage.setContent(content);
                        collectMessage(msg.getFromUserCode(), returnedMessage);
                    }
                })
                .exceptionally(throwable -> {
                    logger.error(ErrorUtil.ERROR_1044 + " message: " + throwable.getMessage());
                    return null;
                });
    }
}
