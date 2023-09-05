package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.model.Message;
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
        communicate(userCode);
    }

    @Override
    public void onClose(String userCode) {
        logger.info("断开连接成功");
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.info(ErrorUtil.ERROR_1018);
            return;
        }
//        userService.logoff(userCode,"", false);
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
        // Check functions
        if (jsonObject.containsKey("functions")) {
            JSONObject functions = jsonObject.getJSONObject("functions");
            if (functions.containsKey("updatePlayerInfo")) {
                playerService.getPlayerInfoMap().put(userCode,
                        functions.getObject("updatePlayerInfo", PlayerInfo.class));
            }
            if (functions.containsKey("updatePlayerInfoByEntities")) {
                playerService.updateplayerinfobyentities(userCode, functions.getJSONObject("updatePlayerInfoByEntities"));
            }
            // Check incoming messages
            JSONArray messages = functions.getJSONArray("addMessages");
            for (Object obj : messages) {
                Message msg = JSON.parseObject(String.valueOf(obj), Message.class);
                if (GamePalConstants.SCOPE_GLOBAL == msg.getScope()) {
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
            // Only create, not consume 23/09/04
            JSONArray drops = functions.getJSONArray("addDrops");
            drops.stream().forEach(obj -> {
                WorldDrop drop = JSON.parseObject(String.valueOf(obj), WorldDrop.class);
                String code = UUID.randomUUID().toString();
                drop.setCode(code);
                drop.setType(GamePalConstants.BLOCK_TYPE_DROP);
                if (world.getBlockMap().containsKey(code)) {
                    logger.warn(ErrorUtil.ERROR_1013 + " code: " + code);
                } else {
                    world.getBlockMap().put(code, drop);
                }
            });
            if (functions.containsKey("useDrop")) {
                // Only consume, not obtain 23/09/04
                JSONObject useDrop = functions.getJSONObject("useDrop");
                String code = useDrop.getString("code");
                if (!world.getBlockMap().containsKey(code)) {
                    logger.warn(ErrorUtil.ERROR_1012);
                }
                world.getBlockMap().remove(code);
            }
            if (functions.containsKey("setRelation")) {
                JSONObject setRelation = functions.getJSONObject("setRelation");
                String userCode1 = setRelation.getString("userCode");
                String userCode2 = setRelation.getString("nextUserCode");
                int newRelation = setRelation.getInteger("newRelation");
                boolean isAbsolute = setRelation.getBoolean("isAbsolute");
                ResponseEntity setRelationRst =
                        playerService.setRelation(userCode1, userCode2, newRelation, isAbsolute);
                if (setRelationRst.getStatusCode().isError()) {
                    logger.warn(setRelationRst);
                }
            }
        }
        // Reply automatically
        communicate(userCode);
    }

    @Override
    public void communicate(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        rst.put("userCode", userCode);
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.warn(ErrorUtil.ERROR_1016 + "userCode: " + userCode);
            logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + userCode);
            return;
        }

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
        // Return relations
        JSONObject relations = new JSONObject();
        relations.putAll(playerService.getRelationMapByUserCode(userCode));
        rst.put("relations", relations);

        // Return region
        Region region = worldService.getRegionMap().get(playerInfo.getRegionNo());
        JSONObject regionObj = new JSONObject();
        regionObj.put("regionNo", region.getRegionNo());
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
                IntegerCoordinate level1 = PlayerUtil.ConvertBlockType2Level(o1.getType());
                IntegerCoordinate level2 = PlayerUtil.ConvertBlockType2Level(o2.getType());
                if (level1.getX() != level2.getX()) {
                    return level1.getX() - level2.getX();
                }
                if (o1.getY() != o2.getY()) {
                    return o1.getY().compareTo(o2.getY());
                }
                return level1.getY() - level2.getY();
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
                    Teleport tel = new Teleport();
                    tel.setType(teleport.getType());
                    tel.setCode(teleport.getCode());
                    tel.setTo(teleport.getTo());
                    tel.setX(teleport.getX());
                    tel.setY(teleport.getY());
                    PlayerUtil.adjustCoordinate(tel,
                            PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(), scene.getSceneCoordinate()),
                            BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    rankingQueue.add(tel);
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
            block.setCode(entry.getValue().getCode());
            block.setY(entry.getValue().getCoordinate().getY());
            block.setX(entry.getValue().getCoordinate().getX());
            PlayerUtil.adjustCoordinate(block,
                    PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                            entry.getValue().getSceneCoordinate()),
                    BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    rankingQueue.add(block);
        });
        // Collect detected special blocks 23/09/05
        Map<String, WorldBlock> blockMap = world.getBlockMap();
        blockMap.entrySet().stream()
                .filter(entry -> PlayerUtil.getCoordinateRelation(sceneCoordinate,
                        entry.getValue().getSceneCoordinate()) != -1)
                .forEach(entry -> {
                    Block block;
                    switch (entry.getValue().getType()) {
                        case GamePalConstants.BLOCK_TYPE_DROP:
                            Drop drop = new Drop();
                            WorldDrop worldDrop = (WorldDrop) entry.getValue();
                            drop.setItemNo(worldDrop.getItemNo());
                            drop.setAmount(worldDrop.getAmount());
                            block = drop;
                            break;
                        case GamePalConstants.BLOCK_TYPE_TELEPORT:
                            Teleport teleport = new Teleport();
                            WorldTeleport worldTeleport = (WorldTeleport) entry.getValue();
                            teleport.setTo(worldTeleport.getTo());
                            block = teleport;
                            break;
                        default:
                            block = new Block();
                            break;
                    }
                    block.setType(entry.getValue().getType());
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
        while (!rankingQueue.isEmpty()) {
            blocks.add(rankingQueue.poll());
        }
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
