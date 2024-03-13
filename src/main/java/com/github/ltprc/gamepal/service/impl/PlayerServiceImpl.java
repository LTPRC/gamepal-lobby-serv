package com.github.ltprc.gamepal.service.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.item.Consumable;
import com.github.ltprc.gamepal.model.item.Junk;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.StateMachineService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.terminal.GameTerminal;
import com.github.ltprc.gamepal.terminal.Terminal;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Integer RELATION_INIT = 0;
    private static final Integer RELATION_MIN = -100;
    private static final Integer RELATION_MAX = 100;
    private static final Log logger = LogFactory.getLog(PlayerServiceImpl.class);
    private Map<String, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> relationMap = new ConcurrentHashMap<>();
    private Set<String> flagSet = new ConcurrentHashSet<>();
    private Map<String, Terminal> terminalMap = new ConcurrentHashMap<>(); // interactionId, terminal

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private StateMachineService stateMachineService;

    @Override
    public ResponseEntity setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!playerInfoMap.containsKey(nextUserCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        relationMap.computeIfAbsent(userCode, k -> relationMap.put(k, new ConcurrentHashMap<>()));
        relationMap.computeIfAbsent(nextUserCode, k -> relationMap.put(k, new ConcurrentHashMap<>()));
        relationMap.get(userCode).computeIfAbsent(nextUserCode, k ->
                relationMap.get(userCode).put(k, RELATION_INIT));
        relationMap.get(nextUserCode).computeIfAbsent(userCode, k ->
                relationMap.get(nextUserCode).put(k, RELATION_INIT));
        if (!isAbsolute) {
            newRelation += relationMap.get(userCode).get(nextUserCode);
        }
        newRelation = Math.min(RELATION_MAX, Math.max(RELATION_MIN, newRelation));
        if (newRelation != relationMap.get(userCode).get(nextUserCode)) {
            generateNotificationMessage(userCode, "你将对" + playerInfoMap.get(nextUserCode).getNickname() + "的关系"
                    + (newRelation > relationMap.get(userCode).get(nextUserCode) ? "提高" : "降低")
                    + "为" + newRelation);
            generateNotificationMessage(nextUserCode, playerInfoMap.get(userCode).getNickname() + "将对你的关系"
                    + (newRelation > relationMap.get(userCode).get(nextUserCode) ? "提高" : "降低")
                    + "为" + newRelation);
            relationMap.get(userCode).put(nextUserCode, newRelation);
            relationMap.get(nextUserCode).put(userCode, newRelation);
        }
        rst.put("userCode", userCode);
        rst.put("nextUserCode", nextUserCode);
        rst.put("relation", newRelation);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity updateplayerinfoCharacter(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        String firstName = req.getString("firstName");
        if (StringUtils.hasText(firstName)) {
            playerInfo.setFirstName(firstName);
        }
        String lastName = req.getString("lastName");
        if (StringUtils.hasText(lastName)) {
            playerInfo.setLastName(lastName);
        }
        String nickname = req.getString("nickname");
        if (StringUtils.hasText(nickname)) {
            playerInfo.setNickname(nickname);
        }
        String nameColor = req.getString("nameColor");
        if (StringUtils.hasText(nameColor)) {
            playerInfo.setNameColor(nameColor);
        }
        String creature = req.getString("creature");
        if (StringUtils.hasText(creature)) {
            playerInfo.setCreature(creature);
        }
        String gender = req.getString("gender");
        if (StringUtils.hasText(gender)) {
            playerInfo.setGender(gender);
        }
        String skinColor = req.getString("skinColor");
        if (StringUtils.hasText(skinColor)) {
            playerInfo.setSkinColor(skinColor);
        }
        String hairstyle = req.getString("hairstyle");
        if (StringUtils.hasText(hairstyle)) {
            playerInfo.setHairstyle(hairstyle);
        }
        String hairColor = req.getString("hairColor");
        if (StringUtils.hasText(hairColor)) {
            playerInfo.setHairColor(hairColor);
        }
        String eyes = req.getString("eyes");
        if (StringUtils.hasText(eyes)) {
            playerInfo.setEyes(eyes);
        }
        String avatar = req.getString("avatar");
        if (StringUtils.hasText(avatar)) {
            playerInfo.setAvatar(avatar);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity updateMovingBlock(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);

        Integer regionNo = req.getInteger("regionNo");
        if (null != regionNo) {
            playerInfo.setRegionNo(regionNo);
        }
        IntegerCoordinate sceneCoordinate = req.getObject("sceneCoordinate", IntegerCoordinate.class);
        if (null != sceneCoordinate) {
            playerInfo.setSceneCoordinate(sceneCoordinate);
        }
        Coordinate coordinate = req.getObject("coordinate", Coordinate.class);
        if (null != coordinate) {
            playerInfo.setCoordinate(coordinate);
        }
        Coordinate speed = req.getObject("speed", Coordinate.class);
        if (null != speed) {
            playerInfo.setSpeed(speed);
        }
        BigDecimal faceDirection = req.getObject("faceDirection", BigDecimal.class);
        if (null != faceDirection) {
            playerInfo.setFaceDirection(faceDirection);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity getPlayerInfo(HttpServletRequest request) {
        JSONObject rst = ContentUtil.generateRst();
        JSONObject req = null;
        try {
            req = ContentUtil.request2JSONObject(request);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1002));
        }
        String userCode = req.getString("userCode");
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        rst.put("playerInfo", playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public Map<String, PlayerInfo> getPlayerInfoMap() {
        return playerInfoMap;
    }

    @Override
    public ResponseEntity generateNotificationMessage(String userCode, String content) {
        Message message = new Message();
        message.setType(GamePalConstants.MESSAGE_TYPE_PRINTED);
        message.setScope(GamePalConstants.SCOPE_SELF);
        message.setToUserCode(userCode);
        message.setContent(content);
        return messageService.sendMessage(userCode, message);
    }

    @Override
    public Map<String, Integer> getRelationMapByUserCode(String userCode) {
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<String, Integer>());
        }
        return relationMap.get(userCode);
    }

    @Override
    public Set<String> getFlagSet() {
        return flagSet;
    }

    @Override
    public Map<String, Terminal> getTerminalMap() {
        return terminalMap;
    }

    @Override
    public ResponseEntity useItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (!StringUtils.hasText(itemNo) || !playerInfo.getItems().containsKey(itemNo)
                || playerInfo.getItems().get(itemNo) == 0 || itemAmount <= 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1020));
        }
        switch (itemNo.charAt(0)) {
            case GamePalConstants.ITEM_CHARACTER_TOOL:
                int toolIndex = worldService.getItemMap().get(itemNo).getItemIndex();
                Set<String> newTools = new ConcurrentSkipListSet<>();
                if (playerInfo.getTools().contains(itemNo)) {
                    playerInfo.getTools().stream()
                            .filter(toolNo -> !itemNo.equals(toolNo))
                            .forEach(newTools::add);
                    playerInfo.setTools(newTools);
                } else if (toolIndex != GamePalConstants.TOOL_INDEX_DEFAULT){
                    playerInfo.getTools().stream()
                            .filter(toolNo -> toolIndex != worldService.getItemMap().get(toolNo).getItemIndex())
                            .forEach(newTools::add);
                    playerInfo.setTools(newTools);
                    playerInfo.getTools().add(itemNo);
                } else {
                    playerInfo.getTools().add(itemNo);
                }
                break;
            case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                int outfitIndex = worldService.getItemMap().get(itemNo).getItemIndex();
                Set<String> newOutfits = new ConcurrentSkipListSet<>();
                if (playerInfo.getOutfits().contains(itemNo)) {
                    playerInfo.getOutfits().stream()
                            .filter(outfitNo -> !itemNo.equals(outfitNo))
                            .forEach(newOutfits::add);
                    playerInfo.setOutfits(newOutfits);
                } else if (outfitIndex != GamePalConstants.OUTFIT_INDEX_DEFAULT){
                    playerInfo.getOutfits().stream()
                            .filter(outfitNo -> outfitIndex != worldService.getItemMap().get(outfitNo).getItemIndex())
                            .forEach(newOutfits::add);
                    playerInfo.setOutfits(newOutfits);
                    playerInfo.getOutfits().add(itemNo);
                } else {
                    playerInfo.getOutfits().add(itemNo);
                }
                break;
            case GamePalConstants.ITEM_CHARACTER_CONSUMABLE:
                useConsumable(userCode, itemNo, itemAmount);
                break;
            case GamePalConstants.ITEM_CHARACTER_MATERIAL:
                break;
            case GamePalConstants.ITEM_CHARACTER_JUNK:
                getItem(userCode, itemNo, -1);
                ((Junk) worldService.getItemMap().get(itemNo)).getMaterials().entrySet().stream()
                        .forEach(entry -> getItem(userCode, entry.getKey(), entry.getValue()));
                break;
            case GamePalConstants.ITEM_CHARACTER_NOTE:
                break;
            case GamePalConstants.ITEM_CHARACTER_RECORDING:
                break;
            default:
                break;
        }
        flagSet.add(GamePalConstants.FLAG_UPDATE_ITEMS);
        flagSet.add(GamePalConstants.FLAG_UPDATE_PRESERVED_ITEMS);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity getItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldItemAmount = playerInfo.getItems().getOrDefault(itemNo, 0);
        playerInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (playerInfo.getItems().get(itemNo) == 0) {
            playerInfo.getTools().remove(itemNo);
            playerInfo.getOutfits().remove(itemNo);
        }
        BigDecimal capacity = playerInfo.getCapacity();
        playerInfo.setCapacity(capacity.add(worldService.getItemMap().get(itemNo).getWeight()
                .multiply(BigDecimal.valueOf(itemAmount))));
        if (itemAmount < 0) {
            generateNotificationMessage(userCode,
                    "失去 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else if (itemAmount > 0) {
            generateNotificationMessage(userCode,
                    "获得 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        flagSet.add(GamePalConstants.FLAG_UPDATE_ITEMS);
        flagSet.add(GamePalConstants.FLAG_UPDATE_PRESERVED_ITEMS);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity getPreservedItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldItemAmount = playerInfo.getPreservedItems().getOrDefault(itemNo, 0);
        playerInfo.getPreservedItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (itemAmount < 0) {
            generateNotificationMessage(userCode,
                    "取出 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else if (itemAmount > 0) {
            generateNotificationMessage(userCode,
                    "存入 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity changeHp(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldHp = playerInfo.getHp();
        int newHp = isAbsolute ? value : oldHp + value;
        playerInfoMap.get(userCode).setHp(Math.max(0, Math.min(newHp, playerInfo.getHpMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity changeVp(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldVp = playerInfo.getVp();
        int newVp = isAbsolute ? value : oldVp + value;
        playerInfoMap.get(userCode).setVp(Math.max(0, Math.min(newVp, playerInfo.getVpMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity changeHunger(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldHunger = playerInfo.getHunger();
        int newHunger = isAbsolute ? value : oldHunger + value;
        playerInfoMap.get(userCode).setHunger(Math.max(0, Math.min(newHunger, playerInfo.getHungerMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity changeThirst(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldThirst = playerInfo.getThirst();
        int newThirst = isAbsolute ? value : oldThirst + value;
        playerInfoMap.get(userCode).setThirst(Math.max(0, Math.min(newThirst, playerInfo.getThirstMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    private ResponseEntity useConsumable(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        for (int i = 0; i < itemAmount; i++) {
            ((Consumable) worldService.getItemMap().get(itemNo)).getEffects().entrySet().stream()
                    .forEach((Map.Entry<String, Integer> entry) -> {
                        switch (entry.getKey()) {
                            case "hp":
                                changeHp(userCode, entry.getValue(), false);
                                break;
                            case "vp":
                                changeVp(userCode, entry.getValue(), false);
                                break;
                            case "hunger":
                                changeHunger(userCode, entry.getValue(), false);
                                break;
                            case "thirst":
                                changeThirst(userCode, entry.getValue(), false);
                                break;
                            default:
                                break;
                        }
                    });
        }
        switch (worldService.getItemMap().get(itemNo).getItemNo()) {
            case "c005":
                changeHp(userCode, 0, true);
                changeVp(userCode, 0, true);
                changeHunger(userCode, 0, true);
                changeThirst(userCode, 0, true);
                break;
            case "c006":
                changeHp(userCode, playerInfoMap.get(userCode).getHpMax(), true);
                changeVp(userCode, playerInfoMap.get(userCode).getVpMax(), true);
                changeHunger(userCode, playerInfoMap.get(userCode).getHungerMax(), true);
                changeThirst(userCode, playerInfoMap.get(userCode).getThirst(), true);
                break;
            case "c007":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_STUNNED] = -1;
                break;
            case "c008":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_STUNNED] = 0;
                break;
            case "c009":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_BLEEDING] = -1;
                break;
            case "c010":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_BLEEDING] = 0;
                break;
            case "c011":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_SICK] = -1;
                break;
            case "c012":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_SICK] = 0;
                break;
            case "c013":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] = -1;
                break;
            case "c014":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] = 0;
                break;
            case "c015":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_BLIND] = -1;
                break;
            case "c016":
                playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_BLIND] = 0;
                break;
            default:
                break;
        }
        getItem(userCode, itemNo, -1 * itemAmount);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity interactBlocks(String userCode, int interactionCode, String id) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        GameWorld world = userService.getWorldByUserCode(userCode);
        WorldBlock block = world.getBlockMap().getOrDefault(id, null);
        switch (interactionCode) {
            case GamePalConstants.INTERACTION_USE:
                switch (block.getType()) {
                    case GamePalConstants.BLOCK_TYPE_TOILET:
                        generateNotificationMessage(userCode, "你方便了一下。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_WORKSHOP:
                        generateNotificationMessage(userCode, "你对于如何使用一无所知。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_GAME:
                        generateNotificationMessage(userCode, "你开启了桌游。");
                        if (!terminalMap.containsKey(id)) {
                            GameTerminal gameTerminal = new GameTerminal(world);
                            gameTerminal.setId(id);
                            gameTerminal.setUserCode(userCode);
                            gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_START);
                            gameTerminal.setOutputs(new ArrayList<>());
                            terminalMap.put(id, gameTerminal);
                        }
                        stateMachineService.gameTerminalInput((GameTerminal) terminalMap.get(id), "1");
                        break;
                    case GamePalConstants.BLOCK_TYPE_COOKER:
                        generateNotificationMessage(userCode, "你对于如何烹饪一无所知。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_SINK:
                        generateNotificationMessage(userCode, "你清洗了一下双手。");
                        break;
                    default:
                        break;
                }
                break;
            case GamePalConstants.INTERACTION_EXCHANGE:
                break;
            case GamePalConstants.INTERACTION_SLEEP:
                playerInfo.setVp(playerInfo.getVpMax());
                generateNotificationMessage(userCode, "你打了一个盹。");
                break;
            case GamePalConstants.INTERACTION_DRINK:
                playerInfo.setThirst(playerInfo.getThirstMax());
                generateNotificationMessage(userCode, "你痛饮了起来。");
                break;
            case GamePalConstants.INTERACTION_DECOMPOSE:
                break;
            case GamePalConstants.INTERACTION_TALK:
                break;
            case GamePalConstants.INTERACTION_ATTACK:
                break;
            case GamePalConstants.INTERACTION_FLIRT:
                break;
            case GamePalConstants.INTERACTION_SET:
                generateNotificationMessage(userCode, "你捯饬了起来。");
                break;
            default:
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    public ResponseEntity updateBuff(String userCode) {
        JSONObject rst = ContentUtil.generateRst();

        // Check death 24/02/23
        if (0 == playerInfoMap.get(userCode).getHp()
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0) {
            changeVp(userCode, 0, true);
            changeHunger(userCode, 0, true);
            changeThirst(userCode, 0, true);
            // Wipe all other buff and skill remaining time
            playerInfoMap.get(userCode).setBuff(new int[GamePalConstants.BUFF_CODE_LENGTH]);
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_DEAD] = GamePalConstants.BUFF_DEFAULT_FRAME_DEAD;
            for (int i = 0; i < playerInfoMap.get(userCode).getSkill().length; i++) {
                playerInfoMap.get(userCode).getSkill()[i][2] = playerInfoMap.get(userCode).getSkill()[i][3];
            }
        } else if (0 < playerInfoMap.get(userCode).getHp()
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_DEAD] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_DEAD] = 0;
        }

        if (playerInfoMap.get(userCode).getHunger() < playerInfoMap.get(userCode).getHungerMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] == 0) {
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = -1;
        } else if (playerInfoMap.get(userCode).getHunger() >= playerInfoMap.get(userCode).getHungerMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_HUNGRY] = 0;
        }

        if (playerInfoMap.get(userCode).getThirst() < playerInfoMap.get(userCode).getThirstMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] == 0) {
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = -1;
        } else if (playerInfoMap.get(userCode).getThirst() >= playerInfoMap.get(userCode).getThirstMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_THIRSTY] = 0;
        }

        if (playerInfoMap.get(userCode).getVp() < playerInfoMap.get(userCode).getVpMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] == 0) {
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = -1;
        } else if (playerInfoMap.get(userCode).getVp() >= playerInfoMap.get(userCode).getVpMax() / 10
                && playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] != 0){
            playerInfoMap.get(userCode).getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] = 0;
        }

        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity useSkill(String userCode, int skillNo, boolean isDown) {
        JSONObject rst = ContentUtil.generateRst();
        if (isDown) {
            // Skill button is pushed down
            if (playerInfoMap.get(userCode).getSkill()[skillNo][2] == 0) {
                if (playerInfoMap.get(userCode).getSkill()[skillNo][1] == GamePalConstants.SKILL_MODE_SEMI_AUTO) {
                    // It must be -1, otherwise it will be triggered automatically 24/03/05
                    playerInfoMap.get(userCode).getSkill()[skillNo][2] = -1;
                } else if (playerInfoMap.get(userCode).getSkill()[skillNo][1] == GamePalConstants.SKILL_MODE_AUTO) {
                    playerInfoMap.get(userCode).getSkill()[skillNo][2] = playerInfoMap.get(userCode).getSkill()[skillNo][3];
                    generateEventBySkill(userCode, skillNo);
                } else {
                    logger.warn(ErrorUtil.ERROR_1029 + " skillMode: " + playerInfoMap.get(userCode).getSkill()[skillNo][1]);
                }
            }
        } else {
            // Skill button is released
            if (playerInfoMap.get(userCode).getSkill()[skillNo][1] == GamePalConstants.SKILL_MODE_SEMI_AUTO) {
                if (playerInfoMap.get(userCode).getSkill()[skillNo][2] == -1) {
                    playerInfoMap.get(userCode).getSkill()[skillNo][2] = playerInfoMap.get(userCode).getSkill()[skillNo][3];
                    generateEventBySkill(userCode, skillNo);
                }
            } else if (playerInfoMap.get(userCode).getSkill()[skillNo][1] == GamePalConstants.SKILL_MODE_AUTO) {
                // Nothing
            } else {
                logger.warn(ErrorUtil.ERROR_1029 + " skillMode: " + playerInfoMap.get(userCode).getSkill()[skillNo][1]);
            }

        }
        return ResponseEntity.ok().body(rst.toString());
    }

    private void generateEventBySkill(String userCode, int skillNo) {
        WorldBlock eventBlock = new WorldBlock();
        eventBlock.setCode(userCode);
        eventBlock.setRegionNo(playerInfoMap.get(userCode).getRegionNo());
        IntegerCoordinate newSceneCoordinate = new IntegerCoordinate();
        newSceneCoordinate.setX(playerInfoMap.get(userCode).getSceneCoordinate().getX());
        newSceneCoordinate.setY(playerInfoMap.get(userCode).getSceneCoordinate().getY());
        eventBlock.setSceneCoordinate(newSceneCoordinate);
        Coordinate newCoordinate = new Coordinate();
        newCoordinate.setX(new BigDecimal(playerInfoMap.get(userCode).getCoordinate().getX().toString()));
        newCoordinate.setY(new BigDecimal(playerInfoMap.get(userCode).getCoordinate().getY().toString()));
        eventBlock.setCoordinate(newCoordinate);
        switch (playerInfoMap.get(userCode).getSkill()[skillNo][0]) {
            case GamePalConstants.SKILL_CODE_SHOOT:
                newCoordinate.setX(newCoordinate.getX().add(BigDecimal.valueOf((Math.random() + 1) * Math.cos(playerInfoMap.get(userCode).getFaceDirection().doubleValue() / 180 * Math.PI))));
                newCoordinate.setY(newCoordinate.getY().subtract(BigDecimal.valueOf((Math.random() + 1) * Math.sin(playerInfoMap.get(userCode).getFaceDirection().doubleValue() / 180 * Math.PI))));
                eventBlock.setCoordinate(newCoordinate);
                PlayerUtil.fixWorldCoordinate(eventBlock, worldService.getRegionMap().get(eventBlock.getRegionNo()));
                eventBlock.setType(GamePalConstants.EVENT_CODE_SHOOT);
                worldService.addEvent(userCode, eventBlock);
                break;
            case GamePalConstants.SKILL_CODE_HIT:
                newCoordinate.setX(newCoordinate.getX().add(BigDecimal.valueOf((Math.random() + 1) * Math.cos(playerInfoMap.get(userCode).getFaceDirection().doubleValue() / 180 * Math.PI))));
                newCoordinate.setY(newCoordinate.getY().subtract(BigDecimal.valueOf((Math.random() + 1) * Math.sin(playerInfoMap.get(userCode).getFaceDirection().doubleValue() / 180 * Math.PI))));
                eventBlock.setCoordinate(newCoordinate);
                PlayerUtil.fixWorldCoordinate(eventBlock, worldService.getRegionMap().get(eventBlock.getRegionNo()));
                eventBlock.setType(GamePalConstants.EVENT_CODE_HIT);
                worldService.addEvent(userCode, eventBlock);
                break;
            case GamePalConstants.SKILL_CODE_BLOCK:
                PlayerUtil.fixWorldCoordinate(eventBlock, worldService.getRegionMap().get(eventBlock.getRegionNo()));
                eventBlock.setType(GamePalConstants.EVENT_CODE_BLOCK);
                worldService.addEvent(userCode, eventBlock);
                break;
            case GamePalConstants.SKILL_CODE_HEAL:
                // Subtracted 0.01 for event under player's feet 24/03/05
                eventBlock.getCoordinate().setY(eventBlock.getCoordinate().getY().subtract(BigDecimal.valueOf(0.01)));
                PlayerUtil.fixWorldCoordinate(eventBlock, worldService.getRegionMap().get(eventBlock.getRegionNo()));
                eventBlock.setType(GamePalConstants.EVENT_CODE_HEAL);
                worldService.addEvent(userCode, eventBlock);
                break;
            default:
                break;
        }
    }
}
