package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.world.GameWorld;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.world.Drop;
import com.github.ltprc.gamepal.model.world.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.service.*;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private static final int STATE_START = 0;
    private static final int STATE_IN_PROGRESS = 1;
    private static final Log logger = LogFactory.getLog(WebSocketServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WorldService worldService;

    @Override
    public void onOpen(Session session, String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        world.getSessionMap().put(userCode, session);
        logger.info("建立连接成功");
        communicate(userCode, STATE_START);
    }

    @Override
    public void onClose(String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        world.getSessionMap().remove(userCode);
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
        GameWorld world = userService.getWorldByUserCode(userCode);
        // Update onlineMap
        world.getOnlineMap().put(userCode, Instant.now().getEpochSecond());
        // Check requests
        if (jsonObject.containsKey("updatePlayerInfo")) {
            updatePlayerInfo(userCode, jsonObject.getObject("updatePlayerInfo", PlayerInfo.class));
        }
        // Reply automatically
        int state = jsonObject.getInteger("state");
        communicate(userCode, state);
    }

    private void updatePlayerInfo(String userCode, PlayerInfo playerInfo) {
        // Receive playerInfo and update
        playerService.getPlayerInfoMap().put(userCode, playerInfo);
    }

    @Override
    public void communicate(String userCode, int state) {
        JSONObject rst = ContentUtil.generateRst();
        rst.put("userCode", userCode);
        GameWorld world = userService.getWorldByUserCode(userCode);

        // Return stored token
        String token = world.getTokenMap().get(userCode);
        rst.put("token", token);

        // Flush messages automatically
        Map<String, Queue<Message>> messageMap = messageService.getMessageMap();
        if (messageMap.containsKey(userCode) && !messageMap.get(userCode).isEmpty()) {
            JSONArray messages = new JSONArray();
            messages.addAll(messageMap.get(userCode));
            messageMap.get(userCode).clear();
            rst.put("messages", messages);
        }

        // Return playerInfos
        Map<String, PlayerInfo> playerInfoMap = playerService.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        rst.put("playerInfos", playerInfoMap);
        // Return drops
        Map<String, Drop> dropMap = playerService.getDropMap();
        rst.put("drops", dropMap);
        // Return relations
        JSONObject relations = new JSONObject();
        relations.putAll(playerService.getRelationMapByUserCode(userCode));
        rst.put("relations", relations);

        // Generate returned block map
        JSONArray blocks = new JSONArray();
        // Put floors and collect walls
        IntegerCoordinate sceneCoordinate = playerInfo.getRegionCoordinate().getSceneCoordinate();
        Region region = worldService.getRegionMap().get(playerInfo.getRegionNo());
        rst.put("height", region.getHeight());
        rst.put("width", region.getWidth());
        Set<Block> rankingSet = new TreeSet<>(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return o1.getY().compareTo(o2.getY());
            }
        });
        for (int i = sceneCoordinate.getY() - 1; i <= sceneCoordinate.getY() + 1; i++) {
            for (int j = sceneCoordinate.getX() - 1; j <= sceneCoordinate.getX() + 1; j++) {
                Scene scene = region.getScenes().get(new IntegerCoordinate(i, j));
                if (null == scene) {
                    continue;
                }
                scene.getBlocks().entrySet().stream().forEach(entry -> {
                    Block block = new Block();
                    block.setType(2);
                    block.setCode(String.valueOf(Math.abs(entry.getValue())));
                    block.setY(BigDecimal.valueOf(entry.getKey().getY()));
                    block.setX(BigDecimal.valueOf(entry.getKey().getX()));
                    if (entry.getValue() < 0) {
                        blocks.add(block);
                    } else {
                        rankingSet.add(block);
                    }
                });
            }
        }
        // Collect playerInfos as blocks
        playerInfoMap.entrySet().stream().forEach(entry -> {
            Block block = new Block();
            block.setType(1);
            block.setCode(entry.getValue().getUserCode());
            block.setY(entry.getValue().getCoordinate().getY());
            block.setX(entry.getValue().getCoordinate().getX());
            PlayerUtil.adjustCoordinate(block,
                    PlayerUtil.getCoordinateRelation(playerInfo.getRegionCoordinate().getSceneCoordinate(),
                    entry.getValue().getRegionCoordinate().getSceneCoordinate()),
                    BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
            rankingSet.add(block);
        });
        // Collect drops as blocks
        dropMap.entrySet().stream().forEach(entry -> {
            Block block = new Block();
            block.setType(1);
            block.setCode(entry.getKey());
            block.setY(entry.getValue().getCoordinate().getY());
            block.setX(entry.getValue().getCoordinate().getX());
            PlayerUtil.adjustCoordinate(block,
                    PlayerUtil.getCoordinateRelation(playerInfo.getRegionCoordinate().getSceneCoordinate(),
                            entry.getValue().getSceneCoordinate()),
                    BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
            rankingSet.add(block);
        });
        // Put all blocks
        blocks.addAll(rankingSet);
        rst.put("blocks", blocks);

        // Communicate
        String content = JSONObject.toJSONString(rst);
        Session session = world.getSessionMap().get(userCode);
        if (null == session || null == session.getBasicRemote()) {
            logger.warn(ErrorUtil.ERROR_1003 + "userCode: " + userCode);
            return;
        }
        try {
            session.getBasicRemote().sendText(content);
        } catch (IOException e) {
            logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + userCode);
        }
    }
}
