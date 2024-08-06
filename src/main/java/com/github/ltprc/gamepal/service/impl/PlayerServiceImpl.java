package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.creature.NpcBrain;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.StateMachineService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.terminal.GameTerminal;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Integer RELATION_INIT = 0;
    private static final Integer RELATION_MIN = -100;
    private static final Integer RELATION_MAX = 100;
    private static final Log logger = LogFactory.getLog(PlayerServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private BuffManager buffManager;

    @Override
    public ResponseEntity<String> updatePlayerInfo(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = JSON.parseObject(String.valueOf(req), PlayerInfo.class);
        playerInfoMap.put(userCode, playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> updatePlayerInfoCharacter(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        Integer playerStatus = req.getInteger("playerStatus");
        if (null != playerStatus) {
            playerInfo.setPlayerStatus(playerStatus);
        }
        String firstName = req.getString("firstName");
        if (StringUtils.isNotBlank(firstName)) {
            playerInfo.setFirstName(firstName);
        }
        String lastName = req.getString("lastName");
        if (StringUtils.isNotBlank(lastName)) {
            playerInfo.setLastName(lastName);
        }
        String nickname = req.getString("nickname");
        if (StringUtils.isNotBlank(nickname)) {
            playerInfo.setNickname(nickname);
        }
        String nameColor = req.getString("nameColor");
        if (StringUtils.isNotBlank(nameColor)) {
            playerInfo.setNameColor(nameColor);
        }
        Integer creatureType = req.getInteger("creatureType");
        if (null != creatureType) {
            playerInfo.setCreatureType(creatureType);
        }
        Integer gender = req.getInteger("gender");
        if (null != gender) {
            playerInfo.setGender(gender);
        }
        Integer skinColor = req.getInteger("skinColor");
        if (null != skinColor) {
            playerInfo.setSkinColor(skinColor);
        }
        Integer hairstyle = req.getInteger("hairstyle");
        if (null != hairstyle) {
            playerInfo.setHairstyle(hairstyle);
        }
        Integer hairColor = req.getInteger("hairColor");
        if (null != hairColor) {
            playerInfo.setHairColor(hairColor);
        }
        Integer eyes = req.getInteger("eyes");
        if (null != eyes) {
            playerInfo.setEyes(eyes);
        }
        JSONArray faceCoefs = req.getJSONArray("faceCoefs");
        if (null != faceCoefs) {
            playerInfo.setFaceCoefs(new int[GamePalConstants.FACE_COEFS_LENGTH]);
            for (int i = 0; i < GamePalConstants.FACE_COEFS_LENGTH; i++) {
                playerInfo.getFaceCoefs()[i] = faceCoefs.getInteger(i);
            }
        }
        String avatar = req.getString("avatar");
        if (StringUtils.isNotBlank(avatar)) {
            playerInfo.setAvatar(avatar);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> updatePlayerMovement(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        PlayerInfo playerMovement = JSON.toJavaObject(req, PlayerInfo.class);
        Integer playerStatus = req.getInteger("playerStatus");
        if (null != playerStatus) {
            playerInfo.setPlayerStatus(playerStatus);
        }
        playerInfo.setRegionNo(playerMovement.getRegionNo());
        IntegerCoordinate sceneCoordinate = playerMovement.getSceneCoordinate();
        if (null != sceneCoordinate) {
            playerInfo.setSceneCoordinate(sceneCoordinate);
        }
        Coordinate coordinate = playerMovement.getCoordinate();
        if (null != coordinate) {
            playerInfo.setCoordinate(coordinate);
        }
        Coordinate speed = playerMovement.getSpeed();
        if (null != speed) {
            playerInfo.setSpeed(speed);
        }
        BigDecimal faceDirection = playerMovement.getFaceDirection();
        if (null != faceDirection) {
            playerInfo.setFaceDirection(faceDirection);
        }
//        JSONObject structureObj = req.getJSONObject("structure");
//        List<Shape> shapes = structureObj.getJSONArray("shapes").stream()
//                .map(shapeObj -> JSON.parseObject(String.valueOf(shapeObj), Shape.class))
//                .collect(Collectors.toList());
//        playerInfo.getStructure().setShapes(shapes);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> generateNotificationMessage(String userCode, String content) {
        Message message = new Message();
        message.setType(GamePalConstants.MESSAGE_TYPE_PRINTED);
        message.setScope(GamePalConstants.SCOPE_SELF);
        message.setToUserCode(userCode);
        message.setContent(content);
        return messageService.sendMessage(userCode, message);
    }

    @Override
    public Map<String, Integer> getRelationMapByUserCode(String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016 + " userCode: " + userCode);
            return new ConcurrentHashMap<>();
        }
        Map<String, Map<String, Integer>> relationMap = world.getRelationMap();
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<>());
        }
        return relationMap.get(userCode);
    }

    @Override
    public ResponseEntity<String> setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        if (userService.getWorldByUserCode(userCode) != userService.getWorldByUserCode(nextUserCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1017));
        }
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        Map<String, Map<String, Integer>> relationMap = world.getRelationMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!playerInfoMap.containsKey(nextUserCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<>());
        }
        if (!relationMap.containsKey(nextUserCode)) {
            relationMap.put(nextUserCode, new ConcurrentHashMap<>());
        }
        if (!relationMap.get(userCode).containsKey(nextUserCode)) {
            relationMap.get(userCode).put(nextUserCode, RELATION_INIT);
        }
        if (!relationMap.get(nextUserCode).containsKey(userCode)) {
            relationMap.get(nextUserCode).put(userCode, RELATION_INIT);
        }
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
    public ResponseEntity<String> useItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        if (!StringUtils.isNotBlank(itemNo) || !bagInfo.getItems().containsKey(itemNo)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1020));
        }
        if (bagInfo.getItems().get(itemNo) == 0 || itemAmount <= 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1024));
        }
        switch (itemNo.charAt(0)) {
            case GamePalConstants.ITEM_CHARACTER_TOOL:
                useTools(userCode, itemNo);
                break;
            case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                useOutfits(userCode, itemNo);
                break;
            case GamePalConstants.ITEM_CHARACTER_CONSUMABLE:
                useConsumable(userCode, itemNo, itemAmount);
                break;
            case GamePalConstants.ITEM_CHARACTER_MATERIAL:
                break;
            case GamePalConstants.ITEM_CHARACTER_JUNK:
                getItem(userCode, itemNo, -1);
                ((Junk) worldService.getItemMap().get(itemNo)).getMaterials().entrySet()
                        .forEach(entry -> getItem(userCode, entry.getKey(), entry.getValue()));
                break;
            case GamePalConstants.ITEM_CHARACTER_NOTE:
                break;
            case GamePalConstants.ITEM_CHARACTER_RECORDING:
                break;
            default:
                break;
        }
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_ITEMS);
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_INTERACTED_ITEMS);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> getItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        if (itemAmount == 0) {
            return ResponseEntity.ok().body(rst.toString());
        }
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        int oldItemAmount = bagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1035));
        }
        bagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (bagInfo.getItems().get(itemNo) == 0) {
            switch (itemNo.charAt(0)) {
                case GamePalConstants.ITEM_CHARACTER_TOOL:
                    playerInfo.getTools().remove(itemNo);
                    break;
                case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                    playerInfo.getOutfits().remove(itemNo);
                    break;
                default:
                    break;
            }
        }
        BigDecimal capacity = bagInfo.getCapacity();
        bagInfo.setCapacity(capacity.add(worldService.getItemMap().get(itemNo).getWeight()
                .multiply(BigDecimal.valueOf(itemAmount))));
        if (itemAmount < 0) {
            generateNotificationMessage(userCode,
                    "失去 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else {
            generateNotificationMessage(userCode,
                    "获得 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_ITEMS);
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_INTERACTED_ITEMS);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> getPreservedItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        if (itemAmount == 0) {
            return ResponseEntity.ok().body(rst.toString());
        }
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, BagInfo> preservedBagInfoMap = world.getPreservedBagInfoMap();
        BagInfo preservedBagInfo = preservedBagInfoMap.get(userCode);
        int oldItemAmount = preservedBagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1035));
        }
        preservedBagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (itemAmount < 0) {
            generateNotificationMessage(userCode,
                    "失去 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else {
            generateNotificationMessage(userCode,
                    "储存 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_ITEMS);
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_INTERACTED_ITEMS);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> getInteractedItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        if (itemAmount == 0) {
            return ResponseEntity.ok().body(rst.toString());
        }
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        int oldItemAmount = bagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1035));
        }
        InteractionInfo interactionInfo = world.getInteractionInfoMap().get(userCode);
        if (null == interactionInfo) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1034));
        }
        String id = interactionInfo.getId();
        WorldBlock block = world.getBlockMap().get(id);
        if (null == block) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1012));
        }
        BagInfo interactedBagInfo;
        int oldInteractedItemAmount;
        switch (block.getType()) {
            case GamePalConstants.BLOCK_TYPE_STORAGE:
                Map<String, BagInfo> preservedBagInfoMap = world.getPreservedBagInfoMap();
                interactedBagInfo = preservedBagInfoMap.get(userCode);
                oldInteractedItemAmount = interactedBagInfo.getItems().getOrDefault(itemNo, 0);
                break;
            case GamePalConstants.BLOCK_TYPE_CONTAINER:
                interactedBagInfo = bagInfoMap.get(id);
                oldInteractedItemAmount = interactedBagInfo.getItems().getOrDefault(itemNo, 0);
                break;
            default:
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1013));
        }
        if (oldInteractedItemAmount - itemAmount < 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1035));
        }
        interactedBagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldInteractedItemAmount - itemAmount, Integer.MAX_VALUE)));
        bagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (bagInfo.getItems().get(itemNo) == 0) {
            switch (itemNo.charAt(0)) {
                case GamePalConstants.ITEM_CHARACTER_TOOL:
                    playerInfo.getTools().remove(itemNo);
                    break;
                case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                    playerInfo.getOutfits().remove(itemNo);
                    break;
                default:
                    break;
            }
        }
        BigDecimal capacity = bagInfo.getCapacity();
        bagInfo.setCapacity(capacity.add(worldService.getItemMap().get(itemNo).getWeight()
                .multiply(BigDecimal.valueOf(itemAmount))));
        if (itemAmount < 0) {
            generateNotificationMessage(userCode,
                    "存入 " + worldService.getItemMap().get(itemNo).getName() + "(" + (-1) * itemAmount + ")");
        } else {
            generateNotificationMessage(userCode,
                    "取出 " + worldService.getItemMap().get(itemNo).getName() + "(" + itemAmount + ")");
        }
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_ITEMS);
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_INTERACTED_ITEMS);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useRecipe(String userCode, String recipeNo, int recipeAmount) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        Map<String, Recipe> recipeMap = worldService.getRecipeMap();
        if (!StringUtils.isNotBlank(recipeNo) || !recipeMap.containsKey(recipeNo)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1023));
        }
        Recipe recipe = recipeMap.get(recipeNo);
        if (recipe.getCost().entrySet().stream()
                .anyMatch(entry -> bagInfo.getItems().get(entry.getKey()) < entry.getValue() * recipeAmount)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1024));
        }
        recipe.getCost().entrySet()
                .forEach(entry -> getItem(userCode, entry.getKey(), - entry.getValue() * recipeAmount));
        recipe.getValue().entrySet()
                .forEach(entry -> getItem(userCode, entry.getKey(), entry.getValue() * recipeAmount));
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_RECIPES);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> damageHp(String userCode, String fromUserCode, int value, boolean isAbsolute) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN
        && !world.getNpcBrainMap().get(userCode).getExemption()[CreatureConstants.NPC_EXEMPTION_ALL]) {
            npcManager.prepare2Attack(world, userCode, fromUserCode);
        }
        return changeHp(userCode, value, isAbsolute);
    }

    @Override
    public ResponseEntity<String> changeHp(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldHp = playerInfo.getHp();
        int newHp = isAbsolute ? value : oldHp + value;
        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] != 0) {
            newHp = Math.max(oldHp, newHp);
        }
        playerInfoMap.get(userCode).setHp(Math.max(0, Math.min(newHp, playerInfo.getHpMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changeVp(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldVp = playerInfo.getVp();
        int newVp = isAbsolute ? value : oldVp + value;
        playerInfoMap.get(userCode).setVp(Math.max(0, Math.min(newVp, playerInfo.getVpMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changeHunger(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldHunger = playerInfo.getHunger();
        int newHunger = isAbsolute ? value : oldHunger + value;
        playerInfoMap.get(userCode).setHunger(Math.max(0, Math.min(newHunger, playerInfo.getHungerMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changeThirst(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldThirst = playerInfo.getThirst();
        int newThirst = isAbsolute ? value : oldThirst + value;
        playerInfoMap.get(userCode).setThirst(Math.max(0, Math.min(newThirst, playerInfo.getThirstMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    private ResponseEntity<String> useConsumable(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        for (int i = 0; i < itemAmount; i++) {
            ((Consumable) worldService.getItemMap().get(itemNo)).getEffects().entrySet()
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

    /**
     * Not all cases need to be implemented
     * @param userCode
     * @param interactionCode
     * @return
     */
    @Override
    public ResponseEntity<String> interactBlocks(String userCode, int interactionCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        InteractionInfo interactionInfo = world.getInteractionInfoMap().get(userCode);
        if (null == interactionInfo) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1034));
        }
        String id = interactionInfo.getId();
        WorldBlock block = world.getBlockMap().get(id);
        if (null == block) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1012));
        }
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_ITEMS);
        world.getFlagMap().get(userCode).add(GamePalConstants.FLAG_UPDATE_INTERACTED_ITEMS);
        switch (interactionCode) {
            case GamePalConstants.INTERACTION_USE:
                switch (block.getType()) {
                    case GamePalConstants.BLOCK_TYPE_TOILET:
                        generateNotificationMessage(userCode, "你正在使用马桶。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_WORKSHOP:
                        generateNotificationMessage(userCode, "你正在使用工作台。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_GAME:
                        generateNotificationMessage(userCode, "你开启了桌游。");
                        if (!world.getTerminalMap().containsKey(id)) {
                            GameTerminal gameTerminal = new GameTerminal(world);
                            gameTerminal.setId(id);
                            gameTerminal.setUserCode(userCode);
                            gameTerminal.setStatus(GamePalConstants.GAME_PLAYER_STATUS_START);
                            gameTerminal.setOutputs(new ArrayList<>());
                            world.getTerminalMap().put(id, gameTerminal);
                        }
                        stateMachineService.gameTerminalInput((GameTerminal) world.getTerminalMap().get(id), "1");
                        break;
                    case GamePalConstants.BLOCK_TYPE_COOKER:
                        generateNotificationMessage(userCode, "你正在使用灶台。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_SINK:
                        generateNotificationMessage(userCode, "你正在使用饮水台。");
                        break;
                    default:
                        break;
                }
                break;
            case GamePalConstants.INTERACTION_EXCHANGE:
                switch (block.getType()) {
                    case GamePalConstants.BLOCK_TYPE_STORAGE:
                        generateNotificationMessage(userCode, "你正在交换个人物品。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_CONTAINER:
                        generateNotificationMessage(userCode, "你正在使用容器。");
                        break;
                    default:
                        break;
                }
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
            case GamePalConstants.INTERACTION_SET:
                generateNotificationMessage(userCode, "你捯饬了起来。");
                break;
            default:
                break;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useSkill(String userCode, int skillNo, boolean isDown) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (null == playerInfoMap.get(userCode).getSkill() || playerInfoMap.get(userCode).getSkill().length <= skillNo) {
            logger.error(ErrorUtil.ERROR_1028 + " skillNo: " + skillNo);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1028));
        }
        if (isDown) {
            // Skill button is pushed down
            if (playerInfoMap.get(userCode).getSkill()[skillNo].getFrame() == 0) {
                if (playerInfoMap.get(userCode).getSkill()[skillNo].getSkillMode() == SkillConstants.SKILL_MODE_SEMI_AUTO) {
                    // It must be -1, otherwise it will be triggered automatically 24/03/05
                    playerInfoMap.get(userCode).getSkill()[skillNo].setFrame(-1);
                } else if (playerInfoMap.get(userCode).getSkill()[skillNo].getSkillMode() == SkillConstants.SKILL_MODE_AUTO) {
                    playerInfoMap.get(userCode).getSkill()[skillNo].setFrame(playerInfoMap.get(userCode).getSkill()[skillNo].getFrameMax());
                    generateEventBySkill(userCode, skillNo);
                } else {
                    logger.warn(ErrorUtil.ERROR_1029 + " skillMode: " + playerInfoMap.get(userCode).getSkill()[skillNo].getSkillMode());
                }
            }
        } else {
            // Skill button is released
            if (playerInfoMap.get(userCode).getSkill()[skillNo].getSkillMode() == SkillConstants.SKILL_MODE_SEMI_AUTO) {
                if (playerInfoMap.get(userCode).getSkill()[skillNo].getFrame() == -1) {
                    playerInfoMap.get(userCode).getSkill()[skillNo].setFrame(playerInfoMap.get(userCode).getSkill()[skillNo].getFrameMax());
                    generateEventBySkill(userCode, skillNo);
                }
            } else if (playerInfoMap.get(userCode).getSkill()[skillNo].getSkillMode() == SkillConstants.SKILL_MODE_AUTO) {
                // Nothing
            } else {
                logger.warn(ErrorUtil.ERROR_1029 + " skillMode: " + playerInfoMap.get(userCode).getSkill()[skillNo].getSkillMode());
            }

        }
        return ResponseEntity.ok().body(rst.toString());
    }

    private void generateEventBySkill(String userCode, int skillNo) {
        Random random = new Random();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016 + " userCode: " + userCode);
            return;
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        WorldCoordinate meleeWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                world.getRegionMap().get(playerInfo.getRegionNo()),
                playerInfo, playerInfo.getFaceDirection(), GamePalConstants.EVENT_MAX_DISTANCE_MELEE);
        WorldCoordinate shootWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                world.getRegionMap().get(playerInfo.getRegionNo()),
                playerInfo, playerInfo.getFaceDirection().add(BigDecimal.valueOf(
                        GamePalConstants.EVENT_MAX_ANGLE_SHOOT.doubleValue() * 2 * (random.nextDouble() - 0.5D))),
                GamePalConstants.EVENT_MAX_DISTANCE_SHOOT);
        switch (playerInfo.getSkill()[skillNo].getSkillCode()) {
            case SkillConstants.SKILL_CODE_BLOCK:
                if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != -1) {
                    playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] = GamePalConstants.BUFF_DEFAULT_FRAME_BLOCKED;
                }
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(playerInfo.getRegionNo()), userCode, GamePalConstants.EVENT_CODE_BLOCK,
                        playerInfo));
                break;
            case SkillConstants.SKILL_CODE_HEAL:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(playerInfo.getRegionNo()), userCode, GamePalConstants.EVENT_CODE_HEAL,
                        playerInfo));
                break;
            case SkillConstants.SKILL_CODE_CHEER:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(playerInfo.getRegionNo()), userCode, GamePalConstants.EVENT_CODE_CHEER,
                        playerInfo));
                break;
            case SkillConstants.SKILL_CODE_CURSE:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(playerInfo.getRegionNo()), userCode, GamePalConstants.EVENT_CODE_CURSE,
                        playerInfo));
                break;
            case SkillConstants.SKILL_CODE_MELEE_HIT:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(meleeWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_MELEE_HIT, meleeWc));
                break;
            case SkillConstants.SKILL_CODE_MELEE_KICK:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(meleeWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_MELEE_KICK, meleeWc));
                break;
            case SkillConstants.SKILL_CODE_MELEE_SCRATCH:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(meleeWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_MELEE_SCRATCH, meleeWc));
                break;
            case SkillConstants.SKILL_CODE_MELEE_CLEAVE:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(meleeWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_MELEE_CLEAVE, meleeWc));
                break;
            case SkillConstants.SKILL_CODE_MELEE_STAB:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(meleeWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_MELEE_STAB, meleeWc));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_HIT:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(shootWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_SHOOT_HIT, shootWc));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_ARROW:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(shootWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_SHOOT_ARROW, shootWc));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_GUN:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(shootWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_SHOOT_SLUG, shootWc));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_SHOTGUN:
                for (int i = 0; i < 10; i++) {
                    WorldCoordinate shootShotgunWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(playerInfo.getRegionNo()), playerInfo, playerInfo.getFaceDirection().add(BigDecimal.valueOf(
                                    GamePalConstants.EVENT_MAX_ANGLE_SHOOT_SHOTGUN.doubleValue() * 2 * (random.nextDouble() - 0.5D))),
                            GamePalConstants.EVENT_MAX_DISTANCE_SHOOT_SHOTGUN);
                    worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                            world.getRegionMap().get(shootWc.getRegionNo()), userCode,
                            GamePalConstants.EVENT_CODE_SHOOT_SLUG, shootShotgunWc));
                }
                break;
            case SkillConstants.SKILL_CODE_SHOOT_MAGNUM:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(shootWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_SHOOT_MAGNUM, shootWc));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_ROCKET:
                worldService.addEvent(userCode, BlockUtil.convertEvent2WorldBlock(
                        world.getRegionMap().get(shootWc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_SHOOT_ROCKET, shootWc));
                break;
            default:
                break;
        }
    }

    @Override
    public String findTopBossId(final String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016);
            return userCode;
        }
        if (!world.getPlayerInfoMap().containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return userCode;
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        while (StringUtils.isNotBlank(playerInfo.getBossId()) && !playerInfo.getBossId().equals(playerInfo.getId())) {
            playerInfo = world.getPlayerInfoMap().get(playerInfo.getBossId());
        }
        return playerInfo.getId();
    }

    @Override
    public ResponseEntity<String> setMember(String userCode, String userCode1, String userCode2) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode1);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        if (!world.getPlayerInfoMap().containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (userCode.equals(userCode1)) {
            if (StringUtils.isBlank(userCode2)) {
                generateNotificationMessage(userCode, "你自立了，自此不为任何人效忠。");
                world.getPlayerInfoMap().get(userCode).setBossId(null);
                world.getPlayerInfoMap().get(userCode).setTopBossId(findTopBossId(userCode));
                return ResponseEntity.ok().body(rst.toString());
            }
            if (!world.getPlayerInfoMap().containsKey(userCode2)) {
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
            }
            String nextUserCodeBossId = userCode2;
            while (StringUtils.isNotBlank(nextUserCodeBossId)) {
                if (nextUserCodeBossId.equals(userCode)) {
                    generateNotificationMessage(userCode, world.getPlayerInfoMap().get(userCode2).getNickname()
                            + "是你的下级，你不可以为其效忠。");
                    return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
                }
                nextUserCodeBossId = world.getPlayerInfoMap().get(nextUserCodeBossId).getBossId();
            }
            generateNotificationMessage(userCode, "你向" + world.getPlayerInfoMap().get(userCode2).getNickname()
                    + "屈从了，自此为其效忠。");
            world.getPlayerInfoMap().get(userCode).setBossId(userCode2);
            world.getPlayerInfoMap().get(userCode).setTopBossId(findTopBossId(userCode));
        } else {
            PlayerInfo playerInfo1 = world.getPlayerInfoMap().get(userCode1);
            if (!playerInfo1.getBossId().equals(userCode)) {
                generateNotificationMessage(userCode, "你无法驱逐" + playerInfo1.getNickname()
                        + "，这不是你的直属下级。");
                return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
            } else if (StringUtils.isNotBlank(userCode2)) {
                generateNotificationMessage(userCode, "你无法指派你的直属下级" + playerInfo1.getNickname()
                        + "向他人效忠。");
                return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
            } else {
                playerInfo1.setBossId("");
                playerInfo1.setTopBossId(findTopBossId(userCode1));
                generateNotificationMessage(userCode, "你驱逐了" + playerInfo1.getNickname()
                        + "，对你的效忠就此终止。");
            }
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useTools(String userCode, String itemNo) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int toolIndex = ((Tool) worldService.getItemMap().get(itemNo)).getItemIndex();
        Set<String> newTools = new ConcurrentSkipListSet<>();
        if (playerInfo.getTools().contains(itemNo)) {
            playerInfo.getTools().stream()
                    .filter(toolNo -> !itemNo.equals(toolNo))
                    .forEach(newTools::add);
            playerInfo.setTools(newTools);
        } else if (toolIndex != GamePalConstants.TOOL_INDEX_DEFAULT){
            playerInfo.getTools().stream()
                    .filter(toolNo -> toolIndex != ((Tool) worldService.getItemMap().get(itemNo)).getItemIndex())
                    .forEach(newTools::add);
            playerInfo.setTools(newTools);
            playerInfo.getTools().add(itemNo);
        } else {
            playerInfo.getTools().add(itemNo);
        }
        SkillUtil.updateHumanSkills(playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useOutfits(String userCode, String itemNo) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int outfitIndex = ((Outfit) worldService.getItemMap().get(itemNo)).getItemIndex();
        Set<String> newOutfits = new ConcurrentSkipListSet<>();
        if (playerInfo.getOutfits().contains(itemNo)) {
            playerInfo.getOutfits().stream()
                    .filter(outfitNo -> !itemNo.equals(outfitNo))
                    .forEach(newOutfits::add);
            playerInfo.setOutfits(newOutfits);
        } else if (outfitIndex != GamePalConstants.OUTFIT_INDEX_DEFAULT){
            playerInfo.getOutfits().stream()
                    .filter(outfitNo -> outfitIndex != ((Outfit) worldService.getItemMap().get(itemNo)).getItemIndex())
                    .forEach(newOutfits::add);
            playerInfo.setOutfits(newOutfits);
            playerInfo.getOutfits().add(itemNo);
        } else {
            playerInfo.getOutfits().add(itemNo);
        }
        SkillUtil.updateHumanSkills(playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> addDrop(String userCode, String itemNo, int amount) {
        ResponseEntity<String> result = getItem(userCode, itemNo, -1 * amount);
        if (result.getStatusCode().isError()) {
            return result;
        }
        Random random = new Random();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        WorldMovingBlock worldMovingBlock = new WorldMovingBlock(playerInfo);
        worldMovingBlock.setFaceDirection(BigDecimal.valueOf(random.nextDouble() * 360));
        Coordinate newSpeed = BlockUtil.locateCoordinateWithDirectionAndDistance(playerInfo.getCoordinate(),
                worldMovingBlock.getFaceDirection(), GamePalConstants.DROP_THROW_RADIUS);
        worldMovingBlock.getSpeed().setX(newSpeed.getX().subtract(worldMovingBlock.getCoordinate().getX()));
        worldMovingBlock.getSpeed().setY(newSpeed.getY().subtract(worldMovingBlock.getCoordinate().getY()));
        movementManager.settleSpeed(userCode, worldMovingBlock);
        worldMovingBlock.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        Region region = world.getRegionMap().get(worldMovingBlock.getRegionNo());
        if (!region.getScenes().containsKey(worldMovingBlock.getSceneCoordinate())) {
            worldService.expandScene(world, worldMovingBlock);
        }
        Scene scene = region.getScenes().get(worldMovingBlock.getSceneCoordinate());
        Drop drop = new Drop(itemNo, amount, new Block(GamePalConstants.BLOCK_TYPE_DROP, UUID.randomUUID().toString(),
                "3000", new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Coordinate(BigDecimal.valueOf(0.5D), BigDecimal.valueOf(0.5D)))),
                worldMovingBlock.getCoordinate()));
        drop.getStructure().setImageSize(new Coordinate(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.5)));
        scene.getBlocks().add(drop);
        WorldDrop worldDrop = new WorldDrop(drop.getItemNo(), drop.getAmount(), worldMovingBlock);
        worldDrop.setType(drop.getType());
        worldDrop.setId(drop.getId());
        worldDrop.setCode(drop.getCode());
        world.getBlockMap().put(drop.getId(), worldDrop);
        return result;
    }

    @Override
    public ResponseEntity<String> updateInteractionInfo(String userCode, InteractionInfo interactionInfo) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        if (null != interactionInfo) {
            world.getInteractionInfoMap().put(userCode, interactionInfo);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> killPlayer(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] != 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1036));
        }
        addPlayerTrophy(userCode, playerInfo.getBuff()[GamePalConstants.BUFF_CODE_ANTI_TROPHY] == 0);
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        changeVp(userCode, 0, true);
        changeHunger(userCode, 0, true);
        changeThirst(userCode, 0, true);
        // Reset all skill remaining time
        for (int i = 0; i < playerInfo.getSkill().length; i++) {
            if (null != playerInfo.getSkill()[i]) {
                playerInfo.getSkill()[i].setFrame(playerInfo.getSkill()[i].getFrameMax());
            }
        }
        if (playerInfo.getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN) {
            world.getOnlineMap().remove(userCode);
            npcManager.resetNpcBrainQueues(userCode);
        }
        WorldEvent worldEvent = BlockUtil.createWorldEvent(userCode, GamePalConstants.EVENT_CODE_DISTURB,
                playerInfo);
        world.getEventQueue().add(worldEvent);
        buffManager.resetBuff(playerInfo);
        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_REALISTIC] != 0) {
            destroyPlayer(userCode);
        } else if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_REVIVED] != 0) {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = GamePalConstants.BUFF_DEFAULT_FRAME_DEAD;
        } else {
            playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = -1;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> revivePlayer(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = 0;
        changeHp(userCode, playerInfo.getHpMax(), true);
        changeVp(userCode, playerInfo.getVpMax(), true);
        changeHunger(userCode, playerInfo.getHungerMax(), true);
        changeThirst(userCode, playerInfo.getThirstMax(), true);
        WorldEvent worldEvent = BlockUtil.createWorldEvent(playerInfo.getId(),
                GamePalConstants.EVENT_CODE_SACRIFICE, playerInfo);
        world.getEventQueue().add(worldEvent);
        buffManager.resetBuff(playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> addPlayerTrophy(String userCode, boolean hasTrophy) {
        JSONObject rst = ContentUtil.generateRst();
        Random random = new Random();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        BagInfo bagInfo = world.getBagInfoMap().get(userCode);

        WorldMovingBlock worldMovingBlock = new WorldMovingBlock(playerInfo);
        worldMovingBlock.setFaceDirection(BigDecimal.valueOf(random.nextDouble() * 360));
        Region region = world.getRegionMap().get(worldMovingBlock.getRegionNo());
        if (!region.getScenes().containsKey(worldMovingBlock.getSceneCoordinate())) {
            worldService.expandScene(world, worldMovingBlock);
        }
        Coordinate newSpeed = BlockUtil.locateCoordinateWithDirectionAndDistance(playerInfo.getCoordinate(),
                worldMovingBlock.getFaceDirection(), GamePalConstants.REMAIN_CONTAINER_THROW_RADIUS);
        worldMovingBlock.getSpeed().setX(newSpeed.getX().subtract(worldMovingBlock.getCoordinate().getX()));
        worldMovingBlock.getSpeed().setY(newSpeed.getY().subtract(worldMovingBlock.getCoordinate().getY()));
        movementManager.settleSpeed(userCode, worldMovingBlock);
        worldMovingBlock.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        String id = UUID.randomUUID().toString();
        String code = "3100";
        Block remainContainer = new Block(GamePalConstants.BLOCK_TYPE_CONTAINER, id, code,
                new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                        new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                new Coordinate(BigDecimal.valueOf(0.5D), BigDecimal.valueOf(0.5D)))),
                worldMovingBlock.getCoordinate());
        remainContainer.getStructure().setImageSize(new Coordinate(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.5)));
        BagInfo newBagInfo = new BagInfo();
        newBagInfo.setId(id);
        world.getBagInfoMap().put(id, newBagInfo);
        Scene scene = region.getScenes().get(worldMovingBlock.getSceneCoordinate());
        scene.getBlocks().add(remainContainer);

        worldMovingBlock.setType(remainContainer.getType());
        worldMovingBlock.setId(remainContainer.getId());
        worldMovingBlock.setCode(remainContainer.getCode());
        world.getBlockMap().put(id, worldMovingBlock);

        if (hasTrophy) {
            Map<String, Integer> itemsMap = new HashMap<>(bagInfo.getItems());
            itemsMap.forEach((key, value) -> {
                getItem(userCode, key, -value);
                getItem(id, key, value);
            });
        }
        switch (playerInfo.getCreatureType()) {
            case CreatureConstants.CREATURE_TYPE_HUMAN:
                break;
            case CreatureConstants.CREATURE_TYPE_ANIMAL:
                switch (playerInfo.getSkinColor()) {
                    case CreatureConstants.SKIN_COLOR_PAOFU:
                    case CreatureConstants.SKIN_COLOR_CAT:
                        getItem(id, "c038", random.nextInt(2));
                        break;
                    case CreatureConstants.SKIN_COLOR_FROG:
                        break;
                    case CreatureConstants.SKIN_COLOR_MONKEY:
                        break;
                    case CreatureConstants.SKIN_COLOR_RACOON:
                        break;
                    case CreatureConstants.SKIN_COLOR_CHICKEN:
                        if (random.nextDouble() < 0.25D) {
                            getItem(id, "c040", random.nextInt(1));
                        }
                        getItem(id, "c031", random.nextInt(2));
                        break;
                    case CreatureConstants.SKIN_COLOR_BUFFALO:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "j037", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.4D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_leather", random.nextInt(3));
                        }
                        getItem(id, "c032", random.nextInt(4));
                        break;
                    case CreatureConstants.SKIN_COLOR_FOX:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2));
                        }
                        getItem(id, "c037", random.nextInt(2));
                        break;
                    case CreatureConstants.SKIN_COLOR_POLAR_BEAR:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4));
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2));
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_SHEEP:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        getItem(id, "c034", random.nextInt(2));
                        break;
                    case CreatureConstants.SKIN_COLOR_TIGER:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4));
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_leather", random.nextInt(3));
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_DOG:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "j063", 1);
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "j193", 1);
                        }
                        if (random.nextDouble() < 0.25D) {
                            getItem(id, "j191", random.nextInt(4));
                        }
                        if (random.nextDouble() < 0.2D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2));
                        }
                        getItem(id, "c037", random.nextInt(2));
                        break;
                    case CreatureConstants.SKIN_COLOR_WOLF:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4));
                        }
                        if (random.nextDouble() < 0.2D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.2D) {
                            getItem(id, "m_leather", random.nextInt(2));
                        }
                        getItem(id, "c037", random.nextInt(3));
                        break;
                    case CreatureConstants.SKIN_COLOR_BOAR:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4));
                        }
                        if (random.nextDouble() < 0.4D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_leather", random.nextInt(3));
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_HORSE:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_bone", random.nextInt(2));
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2));
                        }
                        getItem(id, "c036", random.nextInt(2));
                        break;
                    default:
                        return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1038));
                }
                break;
            default:
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1037));
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> destroyPlayer(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        if (CreatureConstants.PLAYER_TYPE_HUMAN != playerInfo.getPlayerType()) {
            NpcBrain npcBrain = world.getNpcBrainMap().get(userCode);
            if (null != npcBrain) {
                npcBrain.getGreenQueue().clear();
                npcBrain.getYellowQueue().clear();
                npcBrain.getRedQueue().clear();
            }
        }
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        BagInfo bagInfo = world.getBagInfoMap().get(userCode);
        if (null != bagInfo) {
            bagInfo.setCapacity(BigDecimal.ZERO);
            bagInfo.getItems().clear();
        }
        BagInfo preservedBagInfo = world.getPreservedBagInfoMap().get(userCode);
        if (null != preservedBagInfo) {
            preservedBagInfo.setCapacity(BigDecimal.ZERO);
            preservedBagInfo.getItems().clear();
        }
        playerInfo.setFaceDirection(BigDecimal.ZERO);
        world.getPlayerInfoMap().values().stream()
                .filter(playerInfo1 -> !playerInfo1.getId().equals(userCode))
                .filter(playerInfo1 -> StringUtils.isNotBlank(playerInfo1.getBossId()))
                .filter(playerInfo1 -> playerInfo1.getBossId().equals(userCode))
                .forEach(playerInfo1 -> setMember(playerInfo1.getId(), playerInfo1.getId(), ""));
        // TODO Game-over display
        userService.logoff(userCode, "", false);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> checkLevelUp(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        if (playerInfo.getExp() >= playerInfo.getExpMax()) {
            playerInfo.setExp(0);
            playerInfo.setLevel(playerInfo.getLevel() + 1);
            SkillUtil.updateExpMax(playerInfo);
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
