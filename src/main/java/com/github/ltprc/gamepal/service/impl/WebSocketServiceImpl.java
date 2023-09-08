package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
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
import java.util.stream.Collectors;

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
        communicate(userCode, GamePalConstants.GAME_STATE_START);
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
        int gameState = jsonObject.getInteger("gameState");

        // Usercode information
        String userCode = jsonObject.getString("userCode");
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return;
        }
        // Update onlineMap
        world.getOnlineMap().put(userCode, Instant.now().getEpochSecond());
        // Check functions
        if (jsonObject.containsKey("functions")) {
            JSONObject functions = jsonObject.getJSONObject("functions");
            if (functions.containsKey("updatePlayerInfo")) {
                playerService.getPlayerInfoMap().put(userCode,
                        functions.getObject("updatePlayerInfo", PlayerInfo.class));
            }
            if (functions.containsKey("updateplayerinfoCharacter")) {
                playerService.updateplayerinfoCharacter(userCode, functions.getJSONObject("updateplayerinfoCharacter"));
            }
            if (functions.containsKey("useItems")) {
                JSONArray useItems = functions.getJSONArray("useItems");
                useItems.stream().forEach(useItem -> {
                    String itemNo = ((JSONObject) useItem).getString("itemNo");
                    int itemAmount = ((JSONObject) useItem).getInteger("itemAmount");
                    playerService.useItem(userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("getItems")) {
                JSONArray getItems = functions.getJSONArray("getItems");
                getItems.stream().forEach(getItem -> {
                    String itemNo = ((JSONObject) getItem).getString("itemNo");
                    int itemAmount = ((JSONObject) getItem).getInteger("itemAmount");
                    playerService.getItem(userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("getPreservedItems")) {
                JSONArray getPreservedItems = functions.getJSONArray("getPreservedItems");
                getPreservedItems.stream().forEach(getPreservedItem -> {
                    String itemNo = ((JSONObject) getPreservedItem).getString("itemNo");
                    int itemAmount = ((JSONObject) getPreservedItem).getInteger("itemAmount");
                    playerService.getPreservedItem(userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("updateMovingBlock")) {
                playerService.updateMovingBlock(userCode, functions.getJSONObject("updateMovingBlock"));
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
                WorldDrop worldDrop = JSON.parseObject(String.valueOf(obj), WorldDrop.class);
                String id = UUID.randomUUID().toString();
                worldDrop.setId(id);
                worldDrop.setCode("3000");
                worldDrop.setType(GamePalConstants.BLOCK_TYPE_DROP);
                if (world.getBlockMap().containsKey(id)) {
                    logger.warn(ErrorUtil.ERROR_1013 + " id: " + id);
                } else {
                    playerService.getItem(userCode, worldDrop.getItemNo(), -1 * worldDrop.getAmount());
                    Region region = worldService.getRegionMap().get(worldDrop.getRegionNo());
                    Scene scene = region.getScenes().get(worldDrop.getSceneCoordinate());
                    scene.getBlocks().add(PlayerUtil.convertWorldBlock2Block(worldDrop));
                    world.getBlockMap().put(id, worldDrop);
                }
            });
            if (functions.containsKey("useDrop")) {
                // Only consume, not obtain 23/09/04
                JSONObject useDrop = functions.getJSONObject("useDrop");
                String id = useDrop.getString("id");
                if (!world.getBlockMap().containsKey(id)) {
                    logger.warn(ErrorUtil.ERROR_1012);
                } else {
                    WorldDrop worldDrop = (WorldDrop) world.getBlockMap().get(id);
                    playerService.getItem(userCode, worldDrop.getItemNo(), worldDrop.getAmount());
                    Region region = worldService.getRegionMap().get(worldDrop.getRegionNo());
                    Scene scene = region.getScenes().get(worldDrop.getSceneCoordinate());
                    scene.setBlocks(scene.getBlocks().stream()
                            .filter(block -> !id.equals(block.getId())).collect(Collectors.toList()));
                    world.getBlockMap().remove(id);
                }
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
        communicate(userCode, gameState);
    }

    @Override
    public void communicate(String userCode, int gameState) {
        JSONObject rst = ContentUtil.generateRst();

        // Static information
        if (gameState == GamePalConstants.GAME_STATE_START) {
            JSONObject itemsObj = new JSONObject();
            worldService.getItemMap().forEach((key, value) -> itemsObj.put(key, value));
            rst.put("items", itemsObj);
        }

        // Usercode information
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
        // Collect blocks from 9 scenes 23/09/07
        for (int i = sceneCoordinate.getY() - 1; i <= sceneCoordinate.getY() + 1; i++) {
            for (int j = sceneCoordinate.getX() - 1; j <= sceneCoordinate.getX() + 1; j++) {
                Scene scene = region.getScenes().get(new IntegerCoordinate(j, i));
                if (null == scene) {
                    continue;
                }
                scene.getBlocks().stream().forEach(block -> {
                    Block newBlock = PlayerUtil.copyBlock(block);
                    PlayerUtil.adjustCoordinate(newBlock,
                            PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(), scene.getSceneCoordinate()),
                            BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                    rankingQueue.add(newBlock);
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
                    block.setId(entry.getValue().getId());
                    block.setCode(entry.getValue().getCode());
                    block.setY(entry.getValue().getCoordinate().getY());
                    block.setX(entry.getValue().getCoordinate().getX());
                    PlayerUtil.adjustCoordinate(block,
                            PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                                    entry.getValue().getSceneCoordinate()),
                            BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
                            rankingQueue.add(block);
        });
        // Collect detected special blocks from blockMap (duplicated) 23/09/08
//        Map<String, WorldBlock> blockMap = world.getBlockMap();
//        blockMap.entrySet().stream()
//                .filter(entry -> PlayerUtil.getCoordinateRelation(sceneCoordinate,
//                        entry.getValue().getSceneCoordinate()) != -1)
//                .forEach(entry -> {
//                    Block block;
//                    switch (entry.getValue().getType()) {
//                        case GamePalConstants.BLOCK_TYPE_DROP:
//                            Drop drop = new Drop();
//                            WorldDrop worldDrop = (WorldDrop) entry.getValue();
//                            drop.setItemNo(worldDrop.getItemNo());
//                            drop.setAmount(worldDrop.getAmount());
//                            block = drop;
//                            break;
//                        case GamePalConstants.BLOCK_TYPE_TELEPORT:
//                            Teleport teleport = new Teleport();
//                            WorldTeleport worldTeleport = (WorldTeleport) entry.getValue();
//                            teleport.setTo(worldTeleport.getTo());
//                            block = teleport;
//                            break;
//                        default:
//                            block = new Block();
//                            break;
//                    }
//                    block.setType(entry.getValue().getType());
//                    block.setId(entry.getValue().getId());
//                    block.setCode(entry.getValue().getCode());
//                    block.setY(entry.getValue().getCoordinate().getY());
//                    block.setX(entry.getValue().getCoordinate().getX());
//                    PlayerUtil.adjustCoordinate(block,
//                            PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
//                                    entry.getValue().getSceneCoordinate()),
//                            BigDecimal.valueOf(region.getHeight()), BigDecimal.valueOf(region.getWidth()));
//                            rankingQueue.add(block);
//        });
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
