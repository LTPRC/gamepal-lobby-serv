package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.MessageConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.InteractionManager;
import com.github.ltprc.gamepal.manager.ItemManager;
import com.github.ltprc.gamepal.manager.MiniMapManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.block.StructuredBlock;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.scene.SceneInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.websocket.Session;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private static final Log logger = LogFactory.getLog(WebSocketServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private MiniMapManager MiniMapManager;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private InteractionManager interactionManager;

    @Autowired
    private MessageService messageService;

    @Override
    public void onOpen(Session session, String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1019);
            return;
        }
        world.getSessionMap().put(userCode, session);
        resetPlayerBlockMap(userCode);
        logger.info("建立连接成功");
        communicate(userCode, GamePalConstants.WEB_STAGE_START, null);
    }

    @Override
    public void onClose(String userCode) {
        resetPlayerBlockMap(userCode);
        logger.info("断开连接成功");
        userService.logoff(userCode, "", false);
    }

    @Override
    public void onMessage(String message) {
        JSONObject jsonObject = JSON.parseObject(message);
        if (null == jsonObject || !jsonObject.containsKey("userCode")) {
            logger.error(ErrorUtil.ERROR_1008);
            return;
        }
        int webStage = jsonObject.getInteger("webStage");
        long timestamp = System.currentTimeMillis();

        // UserCode information
        String userCode = jsonObject.getString("userCode");
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return;
        }
        Block player = world.getCreatureMap().get(userCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        // Update onlineMap
        worldService.registerOnline(world, userCode, timestamp);

        // Check functions
        JSONObject functions = null;
        if (jsonObject.containsKey("functions")) {
            functions = jsonObject.getJSONObject("functions");
            if (functions.containsKey("updatePlayerInfoCharacter")) {
                playerService.updatePlayerInfoCharacter(userCode, functions.getJSONObject("updatePlayerInfoCharacter"));
            }
            if (functions.containsKey("settleCoordinate")
                    && !world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_MOVEMENT]) {
                JSONObject settleCoordinate = functions.getJSONObject("settleCoordinate");
                WorldCoordinate worldCoordinate = JSON.toJavaObject(settleCoordinate
                        .getJSONObject("worldCoordinate"), WorldCoordinate.class);
                MovementInfo movementInfo = JSON.toJavaObject(settleCoordinate
                        .getJSONObject("movementInfo"), MovementInfo.class);
                movementManager.settleCoordinate(world, player, worldCoordinate, false);
                player.getMovementInfo().setSpeed(movementInfo.getSpeed());
                player.getMovementInfo().setFaceDirection(movementInfo.getFaceDirection());
            }
//            if (functions.containsKey("settleSpeedAndCoordinate")) {
//                JSONObject settleSpeedAndCoordinate = functions.getJSONObject("settleSpeedAndCoordinate");
//                MovementInfo movementInfo = JSON.toJavaObject(settleSpeedAndCoordinate
//                        .getJSONObject("movementInfo"), MovementInfo.class);
//                player.getMovementInfo().setSpeed(movementInfo.getSpeed());
//                player.getMovementInfo().setFaceDirection(movementInfo.getFaceDirection());
//                movementManager.settleSpeedAndCoordinate(world, player, 1);
//                world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_MOVEMENT] = true;
//            }
            if (functions.containsKey("settleAcceleration")) {
                JSONObject settleAcceleration = functions.getJSONObject("settleAcceleration");
                Coordinate accelerationCoordinate = new Coordinate(settleAcceleration.getBigDecimal("x"),
                        settleAcceleration.getBigDecimal("y"), settleAcceleration.getBigDecimal("z"));
                Integer movementMode = settleAcceleration.getInteger("movementMode");
                movementManager.settleAcceleration(world, player, accelerationCoordinate, movementMode);
                world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_MOVEMENT] = true;
            }
            if (functions.containsKey("useItems")) {
                JSONArray useItems = functions.getJSONArray("useItems");
                useItems.forEach(useItem -> {
                    String itemNo = ((JSONObject) useItem).getString("itemNo");
                    int itemAmount = ((JSONObject) useItem).getInteger("itemAmount");
                    itemManager.useItem(world, userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("getItems")) {
                JSONArray getItems = functions.getJSONArray("getItems");
                getItems.forEach(getItem -> {
                    String itemNo = ((JSONObject) getItem).getString("itemNo");
                    int itemAmount = ((JSONObject) getItem).getInteger("itemAmount");
                    itemManager.getItem(world, userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("getPreservedItems")) {
                JSONArray getPreservedItems = functions.getJSONArray("getPreservedItems");
                getPreservedItems.forEach(getPreservedItem -> {
                    String itemNo = ((JSONObject) getPreservedItem).getString("itemNo");
                    int itemAmount = ((JSONObject) getPreservedItem).getInteger("itemAmount");
                    itemManager.getPreservedItem(world, userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("getInteractedItems")) {
                JSONArray getInteractedItems = functions.getJSONArray("getInteractedItems");
                getInteractedItems.forEach(getInteractedItem -> {
                    String itemNo = ((JSONObject) getInteractedItem).getString("itemNo");
                    int itemAmount = ((JSONObject) getInteractedItem).getInteger("itemAmount");
                    itemManager.getInteractedItem(world, userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("recycleItems")) {
                JSONArray recycleItems = functions.getJSONArray("recycleItems");
                recycleItems.forEach(recycleItem -> {
                    String itemNo = ((JSONObject) recycleItem).getString("itemNo");
                    int itemAmount = ((JSONObject) recycleItem).getInteger("itemAmount");
                    itemManager.recycleItem(world, userCode, itemNo, itemAmount);
                });
            }
            if (functions.containsKey("useRecipes")) {
                JSONArray useRecipes = functions.getJSONArray("useRecipes");
                useRecipes.forEach(useRecipe -> {
                    String recipeNo = ((JSONObject) useRecipe).getString("recipeNo");
                    int recipeAmount = ((JSONObject) useRecipe).getInteger("recipeAmount");
                    itemManager.useRecipe(world, userCode, recipeNo, recipeAmount);
                });
            }
            // Collect incoming messages
            JSONArray messages = functions.getJSONArray("addMessages");
            messages.stream()
                    .map(obj -> JSON.parseObject(String.valueOf(obj), Message.class))
                    .forEach(msg -> messageService.collectMessage(userCode, msg));
            JSONArray drops = functions.getJSONArray("addDrops");
            drops.forEach(obj -> {
                String itemNo = ((JSONObject) obj).getString("itemNo");
                int itemAmount = ((JSONObject) obj).getInteger("itemAmount");
                if (itemManager.getItem(world, userCode, itemNo, -1 * itemAmount)) {
                    playerService.addDrop(userCode, itemNo, itemAmount);
                }
            });
//            if (functions.containsKey("useDrop")) {
//                JSONObject useDrop = functions.getJSONObject("useDrop");
//                String id = useDrop.getString("id");
//                playerService.useDrop(userCode, id);
//            }
            if (functions.containsKey("setRelation")) {
                JSONObject setRelation = functions.getJSONObject("setRelation");
                String userCode1 = setRelation.getString("userCode");
                String userCode2 = setRelation.getString("nextUserCode");
                int newRelation = setRelation.getInteger("newRelation");
                boolean isAbsolute = setRelation.getBoolean("isAbsolute");
                val setRelationRst = playerService.setRelation(userCode1, userCode2, newRelation, isAbsolute, true);
                if (setRelationRst.getStatusCode().isError()) {
                    logger.warn(setRelationRst);
                }
            }

            if (functions.containsKey("interactBlocks")) {
                JSONArray interactBlocks = functions.getJSONArray("interactBlocks");
                interactBlocks.forEach(interactBlock ->
                        interactionManager.interactBlocks(world, userCode,
                                ((JSONObject) interactBlock).getInteger("interactionCode")));
            }
//            if (functions.containsKey("terminalInputs")) {
//                JSONArray terminalInputs = functions.getJSONArray("terminalInputs");
//                for (Object terminalInput : terminalInputs) {
//                    String id = ((JSONObject) terminalInput).getString("id");
//                    Terminal terminal = world.getTerminalMap().get(id);
//                    if (null == terminal) {
//                        logger.error(ErrorUtil.ERROR_1021 + " userCode: " + userCode);
//                    } else {
//                        stateMachineService.gameTerminalInput((GameTerminal) terminal, ((JSONObject) terminalInput).getString("content"));
//                    }
//                }
//            }
            if (functions.containsKey("useSkills")) {
                JSONArray useSkills = functions.getJSONArray("useSkills");
                for (int i = 0; i < SkillConstants.SKILL_LENGTH; i++) {
                    playerService.useSkill(userCode, i, (Boolean) useSkills.get(i));
                }
            }
            if (functions.containsKey("setMember")) {
                JSONObject setMember = functions.getJSONObject("setMember");
                String userCode1 = setMember.getString("userCode");
                String userCode2 = setMember.getString("nextUserCode");
                playerService.setMember(userCode, userCode1, userCode2, true);
            }
        }
//        logger.debug("RSP执行耗时: " + (System.currentTimeMillis() - timestamp) + " 毫秒");

        // Reply automatically
        long startTime2 = System.currentTimeMillis();
        communicate(userCode, webStage, functions);
//        logger.debug("COM执行耗时: " + (System.currentTimeMillis() - startTime2) + " 毫秒");
    }

    public void communicate(String userCode, int webStage, JSONObject functions) {
        JSONObject rst = ContentUtil.generateRst();
        long timestamp = System.currentTimeMillis();

        // Static information
        if (webStage == GamePalConstants.WEB_STAGE_START) {
            JSONObject staticDataObj = new JSONObject();
            JSONObject itemsObj = new JSONObject();
            itemsObj.putAll(worldService.getItemMap());
            staticDataObj.put("items", itemsObj);
            JSONObject recipesObj = new JSONObject();
            recipesObj.putAll(worldService.getRecipeMap());
            staticDataObj.put("recipes", recipesObj);
            JSONObject structuresObj = new JSONObject();
            structuresObj.putAll(worldService.getStructureMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> String.valueOf(entry.getKey()),
                            Map.Entry::getValue
                    )));
            staticDataObj.put("structures", structuresObj);
            rst.put("staticData", staticDataObj);
        }

        // UserCode information
        rst.put("userCode", userCode);
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.warn(ErrorUtil.ERROR_1016 + "userCode: " + userCode);
            return;
        }

        // Return stored token
        String token = world.getTokenMap().get(userCode);
        rst.put("token", token);

        // Return worldInfo
        JSONObject worldInfo = new JSONObject();
        worldInfo.put("worldTime", world.getWorldTime());
        worldInfo.put("worldTimeSunriseBegin", GamePalConstants.WORLD_TIME_SUNRISE_BEGIN);
        worldInfo.put("worldTimeSunriseEnd", GamePalConstants.WORLD_TIME_SUNRISE_END);
        worldInfo.put("worldTimeSunsetBegin", GamePalConstants.WORLD_TIME_SUNSET_BEGIN);
        worldInfo.put("worldTimeSunsetEnd", GamePalConstants.WORLD_TIME_SUNSET_END);
        worldInfo.put("windDirection", world.getWindDirection());
        worldInfo.put("windSpeed", world.getWindSpeed());
        rst.put("worldInfo", worldInfo);

        // Flush messages automatically
        Map<String, Queue<Message>> messageMap = world.getMessageMap();
        if (messageMap.containsKey(userCode) && !messageMap.get(userCode).isEmpty()) {
            JSONArray messages = new JSONArray();
            messages.addAll(messageMap.get(userCode).stream()
                    .flatMap(message -> {
                        String content = message.getContent();
                        if (null == content || content.length() <= MessageConstants.CHAT_DISPLAY_LINE_CHAR_SIZE_MAX) {
                            return Stream.of(new Message(message));
                        } else {
                            // 需要拆分 content
                            List<Message> parts = new ArrayList<>();
                            for (int i = 0; i < content.length(); i += MessageConstants.CHAT_DISPLAY_LINE_CHAR_SIZE_MAX) {
                                int end = Math.min(i + MessageConstants.CHAT_DISPLAY_LINE_CHAR_SIZE_MAX, content.length());
                                String subContent = content.substring(i, end);
                                Message part = new Message(message);
                                part.setContent(subContent);
                                parts.add(part);
                            }
                            return parts.stream();
                        }
                    })
                    .collect(Collectors.toList()));
            messageMap.get(userCode).clear();
            rst.put("messages", messages);
        }
        rst.put("textDisplayMap", world.getTextDisplayMap());

        // Return playerInfos
        // Old block format 24/09/30
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return;
        }
        // Only detected active creatures and all human players will be collected 24/08/31
        Block player = creatureMap.get(userCode);
        JSONObject playerInfos = new JSONObject();
        creatureMap.values().stream()
                .filter(player1 -> world.getPlayerInfoMap().get(player1.getBlockInfo().getId()).getPlayerType() == GamePalConstants.PLAYER_TYPE_HUMAN)
                .filter(player1 -> StringUtils.equals(userCode, player1.getBlockInfo().getId())
                        || world.getPlayerInfoMap().get(player1.getBlockInfo().getId()).getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                .forEach(player1 -> playerInfos.put(player1.getBlockInfo().getId(),
                        sceneManager.convertBlock2OldBlockInstance(world, userCode, player1, true, timestamp)));
//                        world.getPlayerInfoMap().get(player1.getBlockInfo().getId())));
        creatureMap.values().stream()
                .filter(player1 -> playerService.validateActiveness(world, player1.getBlockInfo().getId()))
                .filter(player1 -> SkillUtil.isSceneDetected(player, player1.getWorldCoordinate(), 2))
                .forEach(player1 -> playerInfos.put(player1.getBlockInfo().getId(),
                        sceneManager.convertBlock2OldBlockInstance(world, userCode, player1, true, timestamp)));
//                        world.getPlayerInfoMap().get(player1.getBlockInfo().getId())));

        rst.put("bagInfo", world.getBagInfoMap().get(userCode));
        if (world.getInteractionInfoMap().containsKey(userCode)) {
            if (BlockConstants.BLOCK_TYPE_STORAGE == world.getInteractionInfoMap().get(userCode).getType()) {
                rst.put("interactedBagInfo", world.getPreservedBagInfoMap().get(userCode));
            } else if (world.getBagInfoMap().containsKey(world.getInteractionInfoMap().get(userCode).getId())) {
                rst.put("interactedBagInfo", world.getBagInfoMap().get(world.getInteractionInfoMap().get(userCode).getId()));
            }
        }

        // Return relations
        JSONObject relations = new JSONObject();
        relations.putAll(playerService.getRelationMapByUserCode(userCode));
        rst.put("relations", relations);
//        JSONArray terminalOutputs = new JSONArray();
//        world.getTerminalMap().entrySet().stream()
//                .filter(entry -> userCode.equals(entry.getValue().getUserCode()))
//                .forEach(entry -> {
//                    entry.getValue().flushOutput().forEach(output -> {
//                        JSONObject terminalOutput = new JSONObject();
//                        terminalOutput.put("content", output);
//                        terminalOutputs.add(terminalOutput);
//                    });
//                    JSONObject gameOutput = ((GameTerminal) entry.getValue()).getGameOutput();
//                    if (null != gameOutput) {
//                        terminalOutputs.add(gameOutput);
//                    }
//                });
//        if (!terminalOutputs.isEmpty()) {
//            rst.put("terminalOutputs", terminalOutputs);
//        }

        // Return regionInfo not region 24/03/18
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        rst.put("regionInfo", JSON.toJSON(new RegionInfo(region)));

        // Return SceneInfos
        JSONArray sceneInfos = new JSONArray();
        region.getScenes().values().stream()
                .filter(scene -> SkillUtil.isSceneDetected(player, scene.getSceneCoordinate(), 2))
                .forEach(scene -> {
            SceneInfo sceneInfo = new SceneInfo(scene);
            sceneInfos.add(sceneInfo);
            if (scene.getSceneCoordinate().equals(player.getWorldCoordinate().getSceneCoordinate())) {
                rst.put("sceneInfo", JSON.toJSON(sceneInfo));
            }
        });
        rst.put("sceneInfos", sceneInfos);

        // Collect grids and altitudes
        if (world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_REGION]) {
            int[][] grids = sceneManager.collectGridsByUserCode(userCode, 2);
            rst.put("grids", grids);
            BigDecimal[][] altitudes = sceneManager.collectAltitudesByUserCode(userCode, 2);
            rst.put("altitudes", altitudes);
        }

        // Collect blocks
        Queue<StructuredBlock> blockQueue = sceneManager.collectSurroundingBlocks(world, player, 2);
        // Remove not detected blocks
        Map<String, Block> userPlayerBlockMap = world.getPlayerBlockMap().get(userCode);
        Set<String> queueIds = blockQueue.stream()
                .map(StructuredBlock::getBlock)
                .map(Block::getBlockInfo)
                .map(BlockInfo::getId)
                .collect(Collectors.toSet());
        userPlayerBlockMap.keySet().removeIf(blockId -> !queueIds.contains(blockId));
        // Traverse every collected block
        JSONArray blocks = new JSONArray();
        JSONArray blockIdList = new JSONArray();
        interactionManager.focusOnBlock(world, userCode, null);
        while (!CollectionUtils.isEmpty(blockQueue)) {
            Block block = blockQueue.poll().getBlock();
            if (null == block || null == block.getBlockInfo()) {
                blockQueue.clear();
                break;
            }
            if (StringUtils.isNotBlank(block.getBlockInfo().getId())) {
                blockIdList.add(block.getBlockInfo().getId());
            }
            if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER) {
                playerInfos.put(block.getBlockInfo().getId(),
                        sceneManager.convertBlock2OldBlockInstance(world, userCode, block, true, timestamp));
            }

            // Update blocks to be interacted possibly
            if (!world.getInteractionInfoMap().containsKey(userCode)
                    || !StringUtils.equals(block.getBlockInfo().getId(),
                    world.getInteractionInfoMap().get(userCode).getId())) {
                interactionManager.focusOnBlock(world, userCode, block);
            }

            // Filter blocks to be updated and transmitted
            JSONObject convertedBlock = sceneManager.convertBlock2OldBlockInstance(world, userCode, block, false, timestamp);
            if (null != convertedBlock
                    && (BlockConstants.BLOCK_TYPE_PLAYER == block.getBlockInfo().getType()
                    || BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER == block.getBlockInfo().getType()
                    || !userPlayerBlockMap.containsKey(block.getBlockInfo().getId())
                    || userPlayerBlockMap.get(block.getBlockInfo().getId()).getBlockInfo().getTimeUpdated()
                    != block.getBlockInfo().getTimeUpdated())) {
                blocks.add(convertedBlock);
                userPlayerBlockMap.put(block.getBlockInfo().getId(), block);
            }
        }
        rst.put("playerInfos", playerInfos);
        rst.put("blocks", blocks);
        rst.put("blockIdList", blockIdList);

        // Response of functions 24/03/17
        JSONObject functionsResponse = new JSONObject();
        JSONObject miniMap = new JSONObject();
        if (null != functions) {
            if (Boolean.TRUE.equals(functions.getBoolean("createPlayerInfoInstance"))) {
                functionsResponse.put("createPlayerInfoInstance", CreatureFactory.createCreatureInstance(
                        GamePalConstants.PLAYER_TYPE_HUMAN, CreatureConstants.CREATURE_TYPE_HUMAN));
            }
            if (Boolean.TRUE.equals(functions.getBoolean("updateMiniMap"))) {
                JSONArray background = MiniMapManager.generateMiniMapBackground(region,
                        new IntegerCoordinate(GamePalConstants.MINI_MAP_DEFAULT_SIZE, GamePalConstants.MINI_MAP_DEFAULT_SIZE));
                miniMap.put("background", background);
            }
        }
        rst.put("functions", functionsResponse);
        IntegerCoordinate sceneCoordinate = MiniMapManager.getMiniMapSceneCoordinate(region,
                new IntegerCoordinate(GamePalConstants.MINI_MAP_DEFAULT_SIZE, GamePalConstants.MINI_MAP_DEFAULT_SIZE),
                player.getWorldCoordinate().getSceneCoordinate());
        miniMap.put("sceneCoordinate", sceneCoordinate);
        rst.put("miniMap", miniMap);

        // Latest timestamp
        rst.put("currentSecond", Instant.now().getEpochSecond() % 60);
        rst.put("currentMillisecond", Instant.now().getNano() / 1000_000);

        rst.put("interactionInfo", world.getInteractionInfoMap().get(userCode));

        rst.put("flags", world.getFlagMap().get(userCode));
        // Clear flags
        world.getFlagMap().put(userCode, new boolean[FlagConstants.FLAG_LENGTH]);

//        if (timestamp % 10 == 0) {
//            analyzeJsonContent(rst);
//        }
        transmit(rst, userCode, world);
    }

    private void transmit(JSONObject rst, String userCode, GameWorld world) {
        // Communicate
        String content = JSON.toJSONString(rst, SerializerFeature.DisableCircularReferenceDetect);
        Session session = world.getSessionMap().get(userCode);
        if (null == session || null == session.getBasicRemote()) {
            logger.warn(ErrorUtil.ERROR_1003 + "userCode: " + userCode);
            return;
        }
        try {
            session.getBasicRemote().sendText(content);
        } catch (IOException | IllegalStateException e) {
            logger.warn(ErrorUtil.ERROR_1010 + "userCode: " + userCode);
            try {
                session.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static void analyzeJsonContentOld(JSONObject rst) {
        logger.info("Analyze json content now...");
        int totalLength = 0;
        for (String key : rst.keySet()) {
            Object value = rst.get(key);
            String valueAsString = value.toString();
            int length = valueAsString.length();
            logger.info("Key: " + key + ", Value Length: " + length);
            if (key.equals("playerInfos")) {
                JSONObject playerInfos = rst.getJSONObject(key);
                logger.info("playerInfos.count: " + playerInfos.keySet().size());
//                for (String userCode : playerInfos.keySet()) {
//                    logger.info("userCode: " + userCode);
//                    logger.info("playerInfo.size: " + playerInfos.getJSONObject(userCode).toString().length());
////                    for (String key2 : playerInfos.getJSONObject(userCode).keySet()) {
////                        logger.info("playerInfo.Key: " + key2 + ", Value Length: " + String.valueOf(playerInfos.getJSONObject(userCode).get(key2)).length());
////                    }
//                }
            }
            totalLength += length;
        }
        logger.info("Total length of all values: " + totalLength);
    }

    private static void analyzeJsonContent(JSONObject rst) {
        System.out.println("=== JSON 字段尺寸分析 (UTF-8 bytes) ===");
        long totalBytes = 0;

        for (String key : rst.keySet()) {
            Object value = rst.get(key);
            String valueStr = JSONObject.toJSONString(value); // 保持结构

            // 计算 UTF-8 字节长度（这是网络传输的实际大小）
            int byteSize = valueStr.getBytes(StandardCharsets.UTF_8).length;
            totalBytes += byteSize;

            System.out.printf("  %-20s : %6d bytes -> %s%n",
                    ("\"" + key + "\""),
                    byteSize,
                    truncate(valueStr, 50)
            );
        }

        System.out.println("----------------------------------------");
        System.out.println("Total size: " + totalBytes + " bytes");
        System.out.println("Full JSON size: " + rst.toString().getBytes(StandardCharsets.UTF_8).length + " bytes");
        System.out.println("========================================\n");
    }

    // 辅助方法：截断长字符串用于显示
    private static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    @Override
    public void resetPlayerBlockMap(String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        world.getPlayerBlockMap().put(userCode, new HashMap<>());
    }
}
