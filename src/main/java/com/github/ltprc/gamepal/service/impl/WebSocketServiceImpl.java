package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.PlayerInfoFactory;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.model.map.world.WorldDrop;
import com.github.ltprc.gamepal.service.*;
import com.github.ltprc.gamepal.terminal.GameTerminal;
import com.github.ltprc.gamepal.terminal.Terminal;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.PlayerUtil;
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
    private WorldService worldService;

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private PlayerInfoFactory playerInfoFactory;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private MessageService messageService;

    @Override
    public void onOpen(Session session, String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        world.getSessionMap().put(userCode, session);
        logger.info("建立连接成功");
        communicate(userCode, GamePalConstants.WEB_STAGE_START, null);
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
        int webStage = jsonObject.getInteger("webStage");

        // Usercode information
        String userCode = jsonObject.getString("userCode");
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return;
        }
        // Update onlineMap
        world.getOnlineMap().put(userCode, Instant.now().getEpochSecond());

        // Check functions
        JSONObject functions = null;
        if (jsonObject.containsKey("functions")) {
            functions = jsonObject.getJSONObject("functions");
            if (functions.containsKey("updatePlayerInfo")) {
                playerService.updatePlayerinfo(userCode, functions.getObject("updatePlayerInfo", PlayerInfo.class));
            }
            if (functions.containsKey("updateplayerinfoCharacter")) {
                playerService.updatePlayerinfoCharacter(userCode, functions.getJSONObject("updateplayerinfoCharacter"));
            }
            if (functions.containsKey("updateMovingBlock")) {
                playerService.updatePlayerMovement(userCode, functions.getJSONObject("updateMovingBlock"));
            }
            // Detect and expand scenes after updating player's location
            worldService.expandScene(world, world.getPlayerInfoMap().get(userCode));
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
            // Check incoming messages
            JSONArray messages = functions.getJSONArray("addMessages");
            Map<String, Queue<Message>> messageMap = world.getMessageMap();
            for (Object obj : messages) {
                Message msg = JSON.parseObject(String.valueOf(obj), Message.class);
                if (null != msg.getContent() && msg.getContent().indexOf(GamePalConstants.COMMAND_PREFIX) == 0) {
                    // Command detected
                    String commandContent = StringUtils.trim(msg.getContent().substring(1));
                    if (StringUtils.isNotBlank(commandContent)) {
                        messageService.useCommand(userCode, commandContent);
                    }
                } else if (GamePalConstants.SCOPE_GLOBAL == msg.getScope()) {
                    messageMap.entrySet().stream()
                            .filter(entry -> !entry.getKey().equals(msg.getFromUserCode()))
                            .forEach(entry -> entry.getValue().add(msg));
                } else if (GamePalConstants.SCOPE_INDIVIDUAL == msg.getScope()) {
                    if (!messageMap.containsKey(msg.getToUserCode())) {
                        messageMap.put(msg.getToUserCode(), new LinkedList<>());
                    }
                    messageMap.get(msg.getToUserCode()).add(msg);
                }
            }
            JSONArray drops = functions.getJSONArray("addDrops");
            drops.stream().forEach(obj -> {
                WorldDrop worldDrop = JSON.parseObject(String.valueOf(obj), WorldDrop.class);
                String id = UUID.randomUUID().toString();
                worldDrop.setId(id);
                worldDrop.setCode("3000"); // TODO characterize it
                worldDrop.setType(GamePalConstants.BLOCK_TYPE_DROP);
                if (world.getBlockMap().containsKey(id)) {
                    logger.warn(ErrorUtil.ERROR_1013 + " id: " + id);
                } else {
                    playerService.getItem(userCode, worldDrop.getItemNo(), -1 * worldDrop.getAmount());
                    Region region = world.getRegionMap().get(worldDrop.getRegionNo());
                    Scene scene = region.getScenes().get(worldDrop.getSceneCoordinate());
                    scene.getBlocks().add(PlayerUtil.convertWorldBlock2Block(worldDrop));
                    world.getBlockMap().put(id, worldDrop);
                }
            });
            if (functions.containsKey("useDrop")) {
                JSONObject useDrop = functions.getJSONObject("useDrop");
                String id = useDrop.getString("id");
                if (!world.getBlockMap().containsKey(id)) {
                    logger.warn(ErrorUtil.ERROR_1030);
                } else {
                    WorldDrop worldDrop = (WorldDrop) world.getBlockMap().get(id);
                    playerService.getItem(userCode, worldDrop.getItemNo(), worldDrop.getAmount());
                    Region region = world.getRegionMap().get(worldDrop.getRegionNo());
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
                val setRelationRst = playerService.setRelation(userCode1, userCode2, newRelation, isAbsolute);
                if (setRelationRst.getStatusCode().isError()) {
                    logger.warn(setRelationRst);
                }
            }
            if (functions.containsKey("interactBlocks")) {
                JSONArray interactBlocks = functions.getJSONArray("interactBlocks");
                interactBlocks.stream().forEach(interactBlock -> {
                    int interactionCode = ((JSONObject) interactBlock).getInteger("interactionCode");
                    String id = ((JSONObject) interactBlock).getString("id");
                    playerService.interactBlocks(userCode, interactionCode, id);
                });
            }
            // Deprecated 24/03/04
            if (functions.containsKey("addEvents")) {
                JSONArray addEvents = functions.getJSONArray("addEvents");
                addEvents.stream().forEach(addEvent -> {
                    WorldBlock event = new WorldBlock();
                    event.setType(((JSONObject) addEvent).getInteger("type"));
                    event.setCode(((JSONObject) addEvent).getString("code"));
                    event.setRegionNo(((JSONObject) addEvent).getInteger("regionNo"));
                    event.setSceneCoordinate(((JSONObject) addEvent).getObject("sceneCoordinate", IntegerCoordinate.class));
                    event.setCoordinate(((JSONObject) addEvent).getObject("coordinate", Coordinate.class));
                    worldService.addEvent(userCode, event);
                });
            }
            if (functions.containsKey("terminalInputs")) {
                JSONArray terminalInputs = functions.getJSONArray("terminalInputs");
                for (Object terminalInput : terminalInputs) {
                    String id = ((JSONObject) terminalInput).getString("id");
                    Terminal terminal = world.getTerminalMap().get(id);
                    if (null == terminal) {
                        logger.error(ErrorUtil.ERROR_1021 + " userCode: " + userCode);
                    } else {
                        stateMachineService.gameTerminalInput((GameTerminal) terminal, ((JSONObject) terminalInput).getString("content"));
                    }
                }
            }
            if (functions.containsKey("useSkills")) {
                JSONArray useSkills = functions.getJSONArray("useSkills");
                for (int i = 0; i < GamePalConstants.SKILL_LENGTH; i++) {
                    playerService.useSkill(userCode, i, (Boolean) useSkills.get(i));
                }
            }
        }
        // Reply automatically
        communicate(userCode, webStage, functions);
    }

    public void communicate(String userCode, int webStage, JSONObject functions) {
        JSONObject rst = ContentUtil.generateRst();

        // Static information
        if (webStage == GamePalConstants.WEB_STAGE_START) {
            JSONObject itemsObj = new JSONObject();
            itemsObj.putAll(worldService.getItemMap());
            rst.put("items", itemsObj);
        }

        // UserCode information
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
        Map<String, Queue<Message>> messageMap = world.getMessageMap();
        if (messageMap.containsKey(userCode) && !messageMap.get(userCode).isEmpty()) {
            JSONArray messages = new JSONArray();
            messages.addAll(messageMap.get(userCode));
            messageMap.get(userCode).clear();
            rst.put("messages", messages);
        }

        // Return flags
        if (!world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().put(userCode, new HashSet<>());
        }
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_ITEMS);
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_PRESERVED_ITEMS);

        JSONArray flags = new JSONArray();
        world.getFlagMap().get(userCode).stream().forEach(flags::add);
        rst.put("flags", flags);
        world.getFlagMap().get(userCode).clear();

        // Return playerInfos
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        // All playerInfos are provided, but only blocks of running players or player himself will be collected 24/03/16
        rst.put("playerInfos", playerInfoMap);
        // Return relations
        JSONObject relations = new JSONObject();
        relations.putAll(playerService.getRelationMapByUserCode(userCode));
        rst.put("relations", relations);
        JSONArray terminalOutputs = new JSONArray();
        world.getTerminalMap().entrySet().stream()
                .filter(entry -> userCode.equals(entry.getValue().getUserCode()))
                .forEach(entry -> {
                    entry.getValue().flushOutput().stream().forEach(output -> {
                        JSONObject terminalOutput = new JSONObject();
                        terminalOutput.put("content", output);
                        terminalOutputs.add(terminalOutput);
                    });
                    JSONObject gameOutput = ((GameTerminal) entry.getValue()).getGameOutput();
                    if (null != gameOutput) {
                        terminalOutputs.add(gameOutput);
                    }
                });
        if (!terminalOutputs.isEmpty()) {
            rst.put("terminalOutputs", terminalOutputs);
        }

        // Return regionInfo not region 24/03/18
        Region region = world.getRegionMap().get(playerInfo.getRegionNo());
        rst.put("regionInfo", JSON.toJSON(new RegionInfo(region)));

        // Return SceneInfos
        // sceneInfos is almost useless 24/02/22
        JSONArray sceneInfos = new JSONArray();
        region.getScenes().entrySet().stream().forEach(entry -> {
            SceneInfo sceneInfo = new SceneInfo();
            sceneInfo.setName(entry.getValue().getName());
            sceneInfo.setSceneCoordinate(entry.getKey());
            sceneInfos.add(sceneInfo);
            if (entry.getKey().equals(playerInfo.getSceneCoordinate())) {
                rst.put("sceneInfo", JSON.toJSON(sceneInfo));
            }
        });
        rst.put("sceneInfos", sceneInfos);

        // Collect blocks
        Queue<Block> blockQueue = sceneManager.collectBlocksByUserCode(userCode, GamePalConstants.SCENE_SCAN_RADIUS);
        // Poll all blocks
        JSONArray blocks = new JSONArray();
        while (!CollectionUtils.isEmpty(blockQueue)) {
            blocks.add(blockQueue.poll());
        }
        rst.put("blocks", blocks);

        // Response of functions 24/03/17
        JSONObject functionsResponse = new JSONObject();
        if (null != functions) {
            if (functions.containsKey("createPlayerInfoInstance")
                    && Boolean.TRUE.equals(functions.getBoolean("createPlayerInfoInstance"))) {
                functionsResponse.put("createPlayerInfoInstance", playerInfoFactory.createPlayerInfoInstance());
            }
        }
        rst.put("functions", functionsResponse);

        transmit(rst, userCode, world);
    }

    private void transmit(JSONObject rst, String userCode, GameWorld world) {
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
