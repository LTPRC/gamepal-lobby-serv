package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.GamePalConstants;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.world.GameWorld;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.world.Drop;
import com.github.ltprc.gamepal.model.world.PlayerInfo;
import com.github.ltprc.gamepal.service.*;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class WebSocketServiceImpl implements WebSocketService {

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
        communicate(userCode, GamePalConstants.STATE_START);
    }

    @Override
    public void onClose(String userCode) {
        logger.info("断开连接成功");
    }

    @Override
    public void onMessage(String message) {
        JSONObject jsonObject = JSON.parseObject(message);
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
            playerService.getPlayerInfoMap().put(userCode, jsonObject.getObject("updatePlayerInfo", PlayerInfo.class));
        }
        // Check incoming messages
        JSONArray messages = jsonObject.getJSONArray("messages");
        for (Object obj : messages) {
            Message msg = JSON.parseObject(String.valueOf(obj), Message.class);
            if (msg.getScope().equals(MessageServiceImpl.SCOPE_GLOBAL)) {
                messageService.getMessageMap().entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(msg.getFromUserCode()))
                        .forEach(entry -> entry.getValue().add(msg));
            } else {
                if (!messageService.getMessageMap().containsKey(msg.getToUserCode())) {
                    messageService.getMessageMap().put(msg.getToUserCode(), new LinkedList<>());
                }
                messageService.getMessageMap().get(msg.getToUserCode()).add(msg);
            }
        }
        // Check new drop
        if (jsonObject.containsKey("drops")) {
            JSONArray drops = jsonObject.getJSONArray("drops");
            drops.stream().forEach(obj -> {
                Drop drop = JSON.parseObject(String.valueOf(obj), Drop.class);
                String dropCode = UUID.randomUUID().toString();
                drop.setCode(dropCode);
                if (playerService.getDropMap().containsKey(dropCode)) {
                    logger.warn(ErrorUtil.ERROR_1013 + " dropCode: " + dropCode);
                } else {
                    playerService.getDropMap().put(dropCode, drop);
                }
            });
        }
        // Reply automatically
        int state = jsonObject.getInteger("state");
        communicate(userCode, state);
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

        // Return region
        Region region = worldService.getRegionMap().get(playerInfo.getRegionNo());
        JSONObject regionObj = new JSONObject();
        regionObj.put("height", region.getHeight());
        regionObj.put("width", region.getWidth());
        rst.put("region", regionObj);

        // Return SceneInfos
        JSONArray sceneInfos = new JSONArray();
        region.getScenes().entrySet().stream().forEach(entry -> {
            SceneInfo sceneInfo = new SceneInfo();
            sceneInfo.setName(entry.getValue().getName());
            sceneInfo.setSceneCoordinate(entry.getKey());
            sceneInfos.add(sceneInfo);
        });
        rst.put("sceneInfos", sceneInfos);
        // Generate returned block map
        JSONArray blocks = new JSONArray();
        // Put floors and collect walls
        IntegerCoordinate sceneCoordinate = playerInfo.getSceneCoordinate();
        Queue<Block> rankingQueue = new PriorityQueue<>(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (!o1.getType().equals(o2.getType())) {
                    return PlayerUtil.ConvertBlockType2Level(o1.getType()) - PlayerUtil.ConvertBlockType2Level(o2.getType());
                }
                return o1.getY().compareTo(o2.getY());
            }
        });
        // Collect blocks from nine scenes
        for (int i = sceneCoordinate.getY() - 1; i <= sceneCoordinate.getY() + 1; i++) {
            for (int j = sceneCoordinate.getX() - 1; j <= sceneCoordinate.getX() + 1; j++) {
                Scene scene = region.getScenes().get(new IntegerCoordinate(j, i));
                if (null == scene) {
                    continue;
                }
                // Collect walls and grounds
                scene.getBlocks().entrySet().stream().forEach(entry -> {
                    Block block = new Block();
                    block.setCode(String.valueOf(Math.abs(entry.getValue())));
                    block.setY(BigDecimal.valueOf(entry.getKey().getY()));
                    block.setX(BigDecimal.valueOf(entry.getKey().getX()));
                    PlayerUtil.adjustCoordinate(block,
                            PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(), scene.getSceneCoordinate()),
                            BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    if (entry.getValue() < 0) {
                        block.setType(GamePalConstants.BLOCK_TYPE_GROUND);
                        blocks.add(block);
                    } else if (entry.getValue() > 0) {
                        block.setType(GamePalConstants.BLOCK_TYPE_WALL);
                        rankingQueue.add(block);
                    }
                });
                // Collect teleports
                scene.getTeleports().stream().forEach(teleport -> {
                    PlayerUtil.adjustCoordinate(teleport,
                            PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                                    teleport.getTo().getSceneCoordinate()),
                            BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    rankingQueue.add(teleport);
                });
            }
        }
        // Collect detected playerInfos
        // Player block is included 23/09/01
        playerInfoMap.entrySet().stream()
                .filter(entry -> PlayerUtil.getCoordinateRelation(sceneCoordinate,
                        entry.getValue().getSceneCoordinate()) != -1)
                .forEach(entry -> {
            Block block = new Block();
            block.setType(GamePalConstants.BLOCK_TYPE_PLAYER);
            block.setCode(entry.getValue().getUserCode());
            block.setY(entry.getValue().getCoordinate().getY());
            block.setX(entry.getValue().getCoordinate().getX());
            PlayerUtil.adjustCoordinate(block,
                    PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                            entry.getValue().getSceneCoordinate()),
                    BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    rankingQueue.add(block);
        });
        // Collect detected drops
        dropMap.entrySet().stream()
                .filter(entry -> PlayerUtil.getCoordinateRelation(sceneCoordinate,
                        entry.getValue().getSceneCoordinate()) != -1)
                .forEach(entry -> {
            Block block = new Block();
            block.setType(GamePalConstants.BLOCK_TYPE_DROP);
            block.setCode(entry.getKey());
            block.setY(entry.getValue().getCoordinate().getY());
            block.setX(entry.getValue().getCoordinate().getX());
            PlayerUtil.adjustCoordinate(block,
                    PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                            entry.getValue().getSceneCoordinate()),
                    BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    rankingQueue.add(block);
        });
        // Put all blocks
        blocks.addAll(rankingQueue);
        rst.put("blocks", blocks);

        // Communicate
        String content = JSON.toJSONString(rst);
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
