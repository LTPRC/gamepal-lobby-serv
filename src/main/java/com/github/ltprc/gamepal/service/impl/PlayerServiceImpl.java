package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.manager.*;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.creature.*;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
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
import java.util.stream.Collectors;

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

    @Autowired
    private EventManager eventManager;

    @Override
    public ResponseEntity<String> updatePlayerInfoCharacter(String userCode, JSONObject req) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
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
            playerInfo.setFaceCoefs(new int[CreatureConstants.FACE_COEFS_LENGTH]);
            for (int i = 0; i < CreatureConstants.FACE_COEFS_LENGTH; i++) {
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
    public ResponseEntity<String> updatePlayerMovement(String userCode, WorldCoordinate worldCoordinate,
                                                       MovementInfo movementInfo) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        worldService.expandByCoordinate(world, player.getWorldCoordinate(), worldCoordinate, 1);
        movementManager.settleCoordinate(world, player, worldCoordinate);
        player.getMovementInfo().setSpeed(movementInfo.getSpeed());
        player.getMovementInfo().setFaceDirection(movementInfo.getFaceDirection());
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> generateNotificationMessage(String userCode, String content) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        // Only human can receive message 24/09/30
        if (player.getPlayerInfo().getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1039));
        }
        Message message = new Message();
        message.setType(MessageConstants.MESSAGE_TYPE_PRINTED);
        message.setScope(MessageConstants.SCOPE_SELF);
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Map<String, Map<String, Integer>> relationMap = world.getRelationMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!creatureMap.containsKey(nextUserCode)) {
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
            generateNotificationMessage(userCode, "你将对"
                    + creatureMap.get(nextUserCode).getPlayerInfo().getNickname() + "的关系"
                    + (newRelation > relationMap.get(userCode).get(nextUserCode) ? "提高" : "降低")
                    + "为" + newRelation);
            generateNotificationMessage(nextUserCode, creatureMap.get(userCode).getPlayerInfo().getNickname()
                    + "将对你的关系" + (newRelation > relationMap.get(userCode).get(nextUserCode) ? "提高" : "降低")
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
        if (StringUtils.isBlank(itemNo) || !bagInfo.getItems().containsKey(itemNo)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1020));
        }
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0 || itemAmount <= 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1024));
        }
        switch (itemNo.charAt(0)) {
            case ItemConstants.ITEM_CHARACTER_TOOL:
                useTools(userCode, itemNo);
                break;
            case ItemConstants.ITEM_CHARACTER_OUTFIT:
                useOutfits(userCode, itemNo);
                break;
            case ItemConstants.ITEM_CHARACTER_CONSUMABLE:
                useConsumable(userCode, itemNo, itemAmount);
                break;
            case ItemConstants.ITEM_CHARACTER_MATERIAL:
                break;
            case ItemConstants.ITEM_CHARACTER_JUNK:
                getItem(userCode, itemNo, -1);
                ((Junk) worldService.getItemMap().get(itemNo)).getMaterials().entrySet()
                        .forEach(entry -> getItem(userCode, entry.getKey(), entry.getValue()));
                break;
            case ItemConstants.ITEM_CHARACTER_AMMO:
                break;
            case ItemConstants.ITEM_CHARACTER_NOTE:
                break;
            case ItemConstants.ITEM_CHARACTER_RECORDING:
                break;
            default:
                break;
        }
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        BagInfo bagInfo = bagInfoMap.get(userCode);
        int oldItemAmount = bagInfo.getItems().getOrDefault(itemNo, 0);
        if (oldItemAmount + itemAmount < 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1035));
        }
        bagInfo.getItems().put(itemNo, Math.max(0, Math.min(oldItemAmount + itemAmount, Integer.MAX_VALUE)));
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0) {
            switch (itemNo.charAt(0)) {
                case ItemConstants.ITEM_CHARACTER_TOOL:
                    creatureMap.get(userCode).getPlayerInfo().getTools().remove(itemNo);
                    updateSkillsByTool(userCode);
                    break;
                case ItemConstants.ITEM_CHARACTER_OUTFIT:
                    creatureMap.get(userCode).getPlayerInfo().getOutfits().remove(itemNo);
                    updateSkillsByTool(userCode);
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
        if (world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        }
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
        if (world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        }
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
        Map<String, Block> creatureMap = world.getCreatureMap();
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
        Block block = world.getBlockMap().get(id);
        if (null == block) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1012));
        }
        BagInfo interactedBagInfo;
        int oldInteractedItemAmount;
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_STORAGE:
                Map<String, BagInfo> preservedBagInfoMap = world.getPreservedBagInfoMap();
                interactedBagInfo = preservedBagInfoMap.get(userCode);
                oldInteractedItemAmount = interactedBagInfo.getItems().getOrDefault(itemNo, 0);
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
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
        if (bagInfo.getItems().getOrDefault(itemNo, 0) == 0) {
            switch (itemNo.charAt(0)) {
                case ItemConstants.ITEM_CHARACTER_TOOL:
                    creatureMap.get(userCode).getPlayerInfo().getTools().remove(itemNo);
                    break;
                case ItemConstants.ITEM_CHARACTER_OUTFIT:
                    creatureMap.get(userCode).getPlayerInfo().getOutfits().remove(itemNo);
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
        if (world.getFlagMap().containsKey(userCode)) {
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
            world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        }
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
        if (StringUtils.isBlank(recipeNo) || !recipeMap.containsKey(recipeNo)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1023));
        }
        Recipe recipe = recipeMap.get(recipeNo);
        if (recipe.getCost().entrySet().stream()
                .anyMatch(entry -> bagInfo.getItems().getOrDefault(entry.getKey(), 0) < entry.getValue() * recipeAmount)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1024));
        }
        recipe.getCost().entrySet()
                .forEach(entry -> getItem(userCode, entry.getKey(), - entry.getValue() * recipeAmount));
        recipe.getValue().entrySet()
                .forEach(entry -> getItem(userCode, entry.getKey(), entry.getValue() * recipeAmount));
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_RECIPES] = true;
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> damageHp(String userCode, String fromUserCode, int value, boolean isAbsolute) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (creatureMap.get(userCode).getPlayerInfo().getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        int oldHp = playerInfo.getHp();
        int newHp = isAbsolute ? value : oldHp + value;
        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] != 0) {
            newHp = Math.max(oldHp, newHp);
        }
        playerInfo.setHp(Math.max(0, Math.min(newHp, playerInfo.getHpMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changeVp(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        int oldVp = playerInfo.getVp();
        int newVp = isAbsolute ? value : oldVp + value;
        playerInfo.setVp(Math.max(0, Math.min(newVp, playerInfo.getVpMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changeHunger(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        int oldHunger = playerInfo.getHunger();
        int newHunger = isAbsolute ? value : oldHunger + value;
        playerInfo.setHunger(Math.max(0, Math.min(newHunger, playerInfo.getHungerMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changeThirst(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        int oldThirst = playerInfo.getThirst();
        int newThirst = isAbsolute ? value : oldThirst + value;
        playerInfo.setThirst(Math.max(0, Math.min(newThirst, playerInfo.getThirstMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> changePrecision(String userCode, int value, boolean isAbsolute) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        int oldPrecision = playerInfo.getPrecision();
        int newPrecision = isAbsolute ? value : oldPrecision + value;
        playerInfo.setPrecision(Math.max(0, Math.min(newPrecision, playerInfo.getPrecisionMax())));
        return ResponseEntity.ok().body(rst.toString());
    }

    private ResponseEntity<String> useConsumable(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
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
                killPlayer(userCode);
                break;
            case "c006":
                revivePlayer(userCode);
                break;
            case "c007":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_STUNNED] = -1;
                break;
            case "c008":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_STUNNED] = 0;
                break;
            case "c009":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLEEDING] = -1;
                break;
            case "c010":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLEEDING] = 0;
                break;
            case "c011":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SICK] = -1;
                break;
            case "c012":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SICK] = 0;
                break;
            case "c013":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] = -1;
                break;
            case "c014":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] = 0;
                break;
            case "c015":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLIND] = -1;
                break;
            case "c016":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLIND] = 0;
                break;
            case "c063":
                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_RECOVERING]
                        = 15 * GamePalConstants.FRAME_PER_SECOND;
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        InteractionInfo interactionInfo = world.getInteractionInfoMap().get(userCode);
        if (null == interactionInfo) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1034));
        }
        String id = interactionInfo.getId();
        Block block = world.getBlockMap().get(id);
        if (null == block) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1012));
        }
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        switch (interactionCode) {
            case GamePalConstants.INTERACTION_USE:
                switch (block.getBlockInfo().getType()) {
                    case BlockConstants.BLOCK_TYPE_TOILET:
                        generateNotificationMessage(userCode, "你正在使用马桶。");
                        break;
                    case BlockConstants.BLOCK_TYPE_WORKSHOP:
                        generateNotificationMessage(userCode, "你正在使用工作台。");
                        break;
                    case BlockConstants.BLOCK_TYPE_GAME:
                        generateNotificationMessage(userCode, "你开启了桌游。");
                        if (!world.getTerminalMap().containsKey(id)) {
                            GameTerminal gameTerminal = new GameTerminal(world);
                            gameTerminal.setId(id);
                            gameTerminal.setUserCode(userCode);
                            gameTerminal.setStatus(GameConstants.GAME_PLAYER_STATUS_START);
                            gameTerminal.setOutputs(new ArrayList<>());
                            world.getTerminalMap().put(id, gameTerminal);
                        }
                        stateMachineService.gameTerminalInput((GameTerminal) world.getTerminalMap().get(id), "1");
                        break;
                    case BlockConstants.BLOCK_TYPE_COOKER:
                        generateNotificationMessage(userCode, "你正在使用灶台。");
                        break;
                    case BlockConstants.BLOCK_TYPE_SINK:
                        generateNotificationMessage(userCode, "你正在使用饮水台。");
                        break;
                    default:
                        break;
                }
                break;
            case GamePalConstants.INTERACTION_EXCHANGE:
                switch (block.getBlockInfo().getType()) {
                    case BlockConstants.BLOCK_TYPE_STORAGE:
                        generateNotificationMessage(userCode, "你正在交换个人物品。");
                        break;
                    case BlockConstants.BLOCK_TYPE_CONTAINER:
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
            case GamePalConstants.INTERACTION_PACK:
                // 加一个blockCode和打包blockItemNo的映射
                sceneManager.addDropBlock(world, block.getWorldCoordinate(),new AbstractMap.SimpleEntry<>(
                        BlockUtil.convertBlockType2ItemNo(block.getBlockInfo().getType()), 1));
                sceneManager.removeBlock(world, block);
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        if (null == playerInfo.getSkills() || playerInfo.getSkills().size() <= skillNo) {
            logger.error(ErrorUtil.ERROR_1028 + " skillNo: " + skillNo);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1028));
        }
        BagInfo bagInfo = world.getBagInfoMap().get(userCode);
        String ammoCode = playerInfo.getSkills().get(skillNo).getAmmoCode();
        if (StringUtils.isNotBlank(ammoCode) && bagInfo.getItems().getOrDefault(ammoCode, 0) == 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1040));
        }
        if (isDown) {
            // Skill button is pushed down
            if (playerInfo.getSkills().get(skillNo).getFrame() == 0) {
                if (playerInfo.getSkills().get(skillNo).getSkillMode() == SkillConstants.SKILL_MODE_SEMI_AUTO) {
                    // It must be -1, otherwise it will be triggered automatically 24/03/05
                    playerInfo.getSkills().get(skillNo).setFrame(-1);
                } else if (playerInfo.getSkills().get(skillNo).getSkillMode() == SkillConstants.SKILL_MODE_AUTO) {
                    playerInfo.getSkills().get(skillNo).setFrame(playerInfo.getSkills().get(skillNo).getFrameMax());
                    boolean skillResult = generateEventBySkill(userCode, skillNo);
                    if (skillResult && StringUtils.isNotBlank(ammoCode)) {
                        getItem(userCode, ammoCode, -1);
                    }
                } else {
                    logger.warn(ErrorUtil.ERROR_1029 + " skillMode: " + playerInfo.getSkills().get(skillNo).getSkillMode());
                }
            }
        } else {
            // Skill button is released
            if (playerInfo.getSkills().get(skillNo).getSkillMode() == SkillConstants.SKILL_MODE_SEMI_AUTO) {
                if (playerInfo.getSkills().get(skillNo).getFrame() == -1) {
                    playerInfo.getSkills().get(skillNo).setFrame(playerInfo.getSkills().get(skillNo).getFrameMax());
                    boolean skillResult = generateEventBySkill(userCode, skillNo);
                    if (skillResult && StringUtils.isNotBlank(ammoCode)) {
                        getItem(userCode, ammoCode, -1);
                    }
                }
            } else if (playerInfo.getSkills().get(skillNo).getSkillMode() == SkillConstants.SKILL_MODE_AUTO) {
                // Nothing
            } else {
                logger.warn(ErrorUtil.ERROR_1029 + " skillMode: " + playerInfo.getSkills().get(skillNo).getSkillMode());
            }

        }
        return ResponseEntity.ok().body(rst.toString());
    }

    private boolean generateEventBySkill(String userCode, int skillNo) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016 + " userCode: " + userCode);
            return false;
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        WorldCoordinate worldCoordinate = player.getWorldCoordinate();
        PlayerInfo playerInfo = player.getPlayerInfo();
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        Random random = new Random();
        double gaussianValue = random.nextGaussian();
        // 将生成的值转换成指定的均值和标准差
        BigDecimal direction = player.getMovementInfo().getFaceDirection();
        BigDecimal shakingAngle = BigDecimal.valueOf(gaussianValue * (playerInfo.getPrecisionMax() - playerInfo.getPrecision()) / playerInfo.getPrecisionMax());
        if (playerInfo.getSkills().get(skillNo).getSkillType() == SkillConstants.SKILL_TYPE_ATTACK) {
            changePrecision(userCode, -500, false);
        }
        switch (playerInfo.getSkills().get(skillNo).getSkillCode()) {
            case SkillConstants.SKILL_CODE_BLOCK:
                if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != -1) {
                    playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] = GamePalConstants.BUFF_DEFAULT_FRAME_BLOCKED;
                }
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_BLOCK, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_HEAL:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_HEAL, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_CHEER:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_CHEER, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_CURSE:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_CURSE, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_MELEE_HIT:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_MELEE_HIT, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_KICK:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_MELEE_KICK, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_SCRATCH:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_MELEE_SCRATCH, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_CLEAVE:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_MELEE_CLEAVE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_STAB:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_MELEE_STAB, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_HIT:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_HIT, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_ARROW:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_ARROW, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_GUN:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_SLUG, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_SHOTGUN:
                for (int i = 0; i < 10; i++) {
                    eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_SLUG, userCode,
                            BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                    direction.add(shakingAngle)
                                            .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                            * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT_SHOTGUN));
                }
                break;
            case SkillConstants.SKILL_CODE_SHOOT_MAGNUM:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_MAGNUM, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_ROCKET:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_ROCKET, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_FIRE:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_FIRE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT_FIRE_MAX));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_WATER:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SHOOT_WATER, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_SHOOT_WATER));
                break;
            case SkillConstants.SKILL_CODE_LAY:
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_MINE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                eventManager.addEvent(world, GamePalConstants.EVENT_CODE_TAIL_SMOKE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_BUILD:
                WorldCoordinate buildingWorldCoordinate = BlockUtil.locateBuildingCoordinate(region,
                        player.getWorldCoordinate(), direction, SkillConstants.SKILL_RANGE_BUILD);
                BlockInfo blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, "", "",
                        new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_BOTTOM));
                MovementInfo movementInfo = new MovementInfo();
                Block fakeBuilding = new Block(buildingWorldCoordinate, blockInfo, movementInfo);
                if (sceneManager.checkBlockSpace(world, fakeBuilding)) {
                    BlockInfo blockInfo1 = player.getPlayerInfo().getTools().stream()
                            .filter(tool -> null != BlockUtil.convertItemNo2BlockInfo(tool))
                            .map(BlockUtil::convertItemNo2BlockInfo)
                            .findFirst()
                            .orElseGet(null);
                    if (null != blockInfo1) {
                        sceneManager.addOtherBlock(world, blockInfo1.getType(), blockInfo1.getCode(), buildingWorldCoordinate);
                        eventManager.addEvent(world, GamePalConstants.EVENT_CODE_TAIL_SMOKE, userCode, buildingWorldCoordinate);
                        return true;
                    }
                }
                return false;
            default:
                break;
        }
        return true;
    }

    @Override
    public String findTopBossId(final String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016);
            return userCode;
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007);
            return userCode;
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
        while (StringUtils.isNotBlank(playerInfo.getBossId())
                && !playerInfo.getBossId().equals(player.getBlockInfo().getId())) {
            player = creatureMap.get(playerInfo.getBossId());
        }
        return player.getBlockInfo().getId();
    }

    @Override
    public ResponseEntity<String> setMember(String userCode, String userCode1, String userCode2) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode1);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (userCode.equals(userCode1)) {
            if (StringUtils.isBlank(userCode2)) {
                generateNotificationMessage(userCode, "你自立了，自此不为任何人效忠。");
                creatureMap.get(userCode).getPlayerInfo().setBossId(null);
                creatureMap.get(userCode).getPlayerInfo().setTopBossId(findTopBossId(userCode));
                return ResponseEntity.ok().body(rst.toString());
            }
            if (!creatureMap.containsKey(userCode2)) {
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
            }
            String nextUserCodeBossId = userCode2;
            while (StringUtils.isNotBlank(nextUserCodeBossId)) {
                if (nextUserCodeBossId.equals(userCode)) {
                    generateNotificationMessage(userCode, creatureMap.get(userCode2).getPlayerInfo().getNickname()
                            + "是你的下级，你不可以为其效忠。");
                    return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
                }
                nextUserCodeBossId = creatureMap.get(nextUserCodeBossId).getPlayerInfo().getBossId();
            }
            generateNotificationMessage(userCode, "你向" + creatureMap.get(userCode2).getPlayerInfo().getNickname()
                    + "屈从了，自此为其效忠。");
            creatureMap.get(userCode).getPlayerInfo().setBossId(userCode2);
            creatureMap.get(userCode).getPlayerInfo().setTopBossId(findTopBossId(userCode));
        } else {
            PlayerInfo playerInfo1 = creatureMap.get(userCode1).getPlayerInfo();
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
        int toolIndex = ((Tool) worldService.getItemMap().get(itemNo)).getItemIndex();
        Set<String> newTools = new ConcurrentSkipListSet<>();
        if (playerInfo.getTools().contains(itemNo)) {
            playerInfo.getTools().stream()
                    .filter(toolNo -> !itemNo.equals(toolNo))
                    .forEach(newTools::add);
            playerInfo.setTools(newTools);
        } else if (toolIndex != ItemConstants.TOOL_INDEX_DEFAULT) {
            playerInfo.getTools().stream()
                    .filter(toolNo -> toolIndex != ((Tool) worldService.getItemMap().get(itemNo)).getItemIndex())
                    .forEach(newTools::add);
            playerInfo.setTools(newTools);
            playerInfo.getTools().add(itemNo);
        } else {
            playerInfo.getTools().add(itemNo);
        }
        updateSkillsByTool(userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useOutfits(String userCode, String itemNo) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, Block> creatureMap = world.getCreatureMap();
        PlayerInfo playerInfo = creatureMap.get(userCode).getPlayerInfo();
        int outfitIndex = ((Outfit) worldService.getItemMap().get(itemNo)).getItemIndex();
        Set<String> newOutfits = new ConcurrentSkipListSet<>();
        if (playerInfo.getOutfits().contains(itemNo)) {
            playerInfo.getOutfits().stream()
                    .filter(outfitNo -> !itemNo.equals(outfitNo))
                    .forEach(newOutfits::add);
            playerInfo.setOutfits(newOutfits);
        } else if (outfitIndex != ItemConstants.OUTFIT_INDEX_DEFAULT) {
            playerInfo.getOutfits().stream()
                    .filter(outfitNo -> outfitIndex != ((Outfit) worldService.getItemMap().get(itemNo)).getItemIndex())
                    .forEach(newOutfits::add);
            playerInfo.setOutfits(newOutfits);
            playerInfo.getOutfits().add(itemNo);
        } else {
            playerInfo.getOutfits().add(itemNo);
        }
        updateSkillsByTool(userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> addDrop(String userCode, String itemNo, int amount) {
        JSONObject rst = ContentUtil.generateRst();
        Random random = new Random();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        Block drop = sceneManager.addDropBlock(world, player.getWorldCoordinate(),
                new AbstractMap.SimpleEntry<>(itemNo, amount));
//        BlockInfo dropBlockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_DROP, UUID.randomUUID().toString(),
//                "3000", new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
//                BlockConstants.STRUCTURE_LAYER_MIDDLE,
//                new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
//                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
//                        new Coordinate(BigDecimal.valueOf(0.5D), BigDecimal.valueOf(0.5D))),
//                new Coordinate(BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.5))));
//        WorldCoordinate dropWorldCoordinate = new WorldCoordinate(player.getWorldCoordinate());
//        MovementInfo dropMovementInfo = new MovementInfo();
        MovementInfo dropMovementInfo = drop.getMovementInfo();
        dropMovementInfo.setFaceDirection(BigDecimal.valueOf(random.nextDouble() * 360));
        Coordinate newSpeed = BlockUtil.locateCoordinateWithDirectionAndDistance(new Coordinate(),
                dropMovementInfo.getFaceDirection(), GamePalConstants.DROP_THROW_RADIUS);
        dropMovementInfo.setSpeed(newSpeed);
//        Block drop = new Block(dropWorldCoordinate, dropBlockInfo, dropMovementInfo);
        movementManager.settleSpeedAndCoordinate(world, drop, 0);
        dropMovementInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
//        Region region = world.getRegionMap().get(dropWorldCoordinate.getRegionNo());
//        Scene scene = region.getScenes().get(dropWorldCoordinate.getSceneCoordinate());
//        scene.getBlocks().add(drop);
//        world.getBlockMap().put(dropBlockInfo.getId(), drop);
//        world.getDropMap().put(dropBlockInfo.getId(), new AbstractMap.SimpleEntry<>(itemNo, amount));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useDrop(String userCode, String dropId) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        if (!world.getBlockMap().containsKey(dropId)) {
            logger.warn(ErrorUtil.ERROR_1030);
        } else {
            Map.Entry<String, Integer> drop = world.getDropMap().get(dropId);
            getItem(userCode, drop.getKey(), drop.getValue());
            Block dropBlock = world.getBlockMap().get(dropId);
//            Region region = world.getRegionMap().get(dropBlock.getWorldCoordinate().getRegionNo());
//            Scene scene = region.getScenes().get(dropBlock.getWorldCoordinate().getSceneCoordinate());
//            scene.setBlocks(scene.getBlocks().stream()
//                    .filter(block -> !dropId.equals(block.getBlockInfo().getId())).collect(Collectors.toList()));
//            world.getBlockMap().remove(dropId);
            sceneManager.removeBlock(world, dropBlock);
        }
        return ResponseEntity.ok().body(rst.toString());
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] != 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1036));
        }
        addPlayerTrophy(userCode, playerInfo.getBuff()[GamePalConstants.BUFF_CODE_ANTI_TROPHY] == 0);
        player.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        changeVp(userCode, 0, true);
        changeHunger(userCode, 0, true);
        changeThirst(userCode, 0, true);
        changePrecision(userCode, 0, true);
        // Reset all skill remaining time
        for (int i = 0; i < playerInfo.getSkills().size(); i++) {
            if (null != playerInfo.getSkills().get(i)) {
                playerInfo.getSkills().get(i).setFrame(playerInfo.getSkills().get(i).getFrameMax());
            }
        }
        if (playerInfo.getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN) {
            npcManager.resetNpcBrainQueues(userCode);
        }
        eventManager.addEvent(world, GamePalConstants.EVENT_CODE_DISTURB, userCode, player.getWorldCoordinate());
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
        playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] = 0;
        changeHp(userCode, playerInfo.getHpMax(), true);
        changeVp(userCode, playerInfo.getVpMax(), true);
        changeHunger(userCode, playerInfo.getHungerMax(), true);
        changeThirst(userCode, playerInfo.getThirstMax(), true);
        changePrecision(userCode, playerInfo.getThirstMax(), true);
        eventManager.addEvent(world, GamePalConstants.EVENT_CODE_SACRIFICE, userCode, player.getWorldCoordinate());

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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
        BagInfo bagInfo = world.getBagInfoMap().get(userCode);

        Block remainContainer = sceneManager.addOtherBlock(world, BlockConstants.BLOCK_TYPE_CONTAINER, "3101", player.getWorldCoordinate());
        String id = remainContainer.getBlockInfo().getId();
        worldService.registerOnline(world, remainContainer.getBlockInfo());
        remainContainer.getBlockInfo().getStructure().setMaterial(BlockConstants.STRUCTURE_MATERIAL_MAGNUM); // Special container 24/10/20
//        WorldCoordinate worldCoordinate = remainContainer.getWorldCoordinate();
//        MovementInfo movementInfo = remainContainer.getMovementInfo();
//        movementInfo.setFaceDirection(BigDecimal.valueOf(random.nextDouble() * 360));
//        Coordinate newSpeed = BlockUtil.locateCoordinateWithDirectionAndDistance(worldCoordinate.getCoordinate(),
//                movementInfo.getFaceDirection(), GamePalConstants.REMAIN_CONTAINER_THROW_RADIUS);
//        movementInfo.getSpeed().setX(newSpeed.getX().subtract(worldCoordinate.getCoordinate().getX()));
//        movementInfo.getSpeed().setY(newSpeed.getY().subtract(worldCoordinate.getCoordinate().getY()));
//        movementManager.settleSpeedAndCoordinate(world, remainContainer, 0);
//        movementInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));

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
                        getItem(id, "c038", random.nextInt(2) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_FROG:
                    case CreatureConstants.SKIN_COLOR_MONKEY:
                    case CreatureConstants.SKIN_COLOR_RACOON:
                        getItem(id, "m_leather", 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_CHICKEN:
                        if (random.nextDouble() < 0.25D) {
                            getItem(id, "c040", random.nextInt(1) + 1);
                        }
                        getItem(id, "c031", random.nextInt(2) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_BUFFALO:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "j037", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.4D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_leather", random.nextInt(3) + 1);
                        }
                        getItem(id, "c032", random.nextInt(4) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_FOX:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2) + 1);
                        }
                        getItem(id, "c037", random.nextInt(2) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_POLAR_BEAR:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4) + 1);
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2) + 1);
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_SHEEP:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        getItem(id, "c034", random.nextInt(2) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_TIGER:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4) + 1);
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_leather", random.nextInt(3) + 1);
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
                            getItem(id, "j191", random.nextInt(4) + 1);
                        }
                        if (random.nextDouble() < 0.2D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2) + 1);
                        }
                        getItem(id, "c037", random.nextInt(2) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_WOLF:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4) + 1);
                        }
                        if (random.nextDouble() < 0.2D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.2D) {
                            getItem(id, "m_leather", random.nextInt(2) + 1);
                        }
                        getItem(id, "c037", random.nextInt(3) + 1);
                        break;
                    case CreatureConstants.SKIN_COLOR_BOAR:
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "j191", random.nextInt(4) + 1);
                        }
                        if (random.nextDouble() < 0.4D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.5D) {
                            getItem(id, "m_leather", random.nextInt(3) + 1);
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_HORSE:
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_bone", random.nextInt(2) + 1);
                        }
                        if (random.nextDouble() < 0.1D) {
                            getItem(id, "m_leather", random.nextInt(2) + 1);
                        }
                        getItem(id, "c036", random.nextInt(2) + 1);
                        break;
                    default:
                        return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1038));
                }
                break;
            default:
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1037));
        }
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_ITEMS] = true;
        world.getFlagMap().get(userCode)[FlagConstants.FLAG_UPDATE_INTERACTED_ITEMS] = true;
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> destroyPlayer(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = player.getPlayerInfo();
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
            bagInfo.getItems().clear();
            bagInfo.setCapacity(BigDecimal.ZERO);
            bagInfo.setCapacityMax(BigDecimal.valueOf(CreatureConstants.CAPACITY_MAX));
        }
        BagInfo preservedBagInfo = world.getPreservedBagInfoMap().get(userCode);
        if (null != preservedBagInfo) {
            preservedBagInfo.getItems().clear();
            preservedBagInfo.setCapacity(BigDecimal.ZERO);
            bagInfo.setCapacityMax(BigDecimal.valueOf(CreatureConstants.CAPACITY_MAX));
        }
        player.getMovementInfo().setFaceDirection(CreatureConstants.FACE_DIRECTION_DEFAULT);
        creatureMap.values().stream()
                .filter(player1 -> !player1.getBlockInfo().getId().equals(userCode))
                .filter(player1 -> StringUtils.isNotBlank(player1.getPlayerInfo().getBossId()))
                .filter(player1 -> player1.getPlayerInfo().getBossId().equals(userCode))
                .forEach(player1 -> setMember(player1.getBlockInfo().getId(), player1.getBlockInfo().getId(), ""));
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
        PlayerInfo playerInfo = world.getCreatureMap().get(userCode).getPlayerInfo();
        if (playerInfo.getExp() >= playerInfo.getExpMax()) {
            playerInfo.setExp(0);
            playerInfo.setLevel(playerInfo.getLevel() + 1);
            SkillUtil.updateExpMax(playerInfo);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> updateSkillsByTool(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        PlayerInfo playerInfo = world.getCreatureMap().get(userCode).getPlayerInfo();
        SkillUtil.updateHumanSkills(playerInfo);
        Tool tool = playerInfo.getTools().stream()
                .filter(toolStr -> worldService.getItemMap().containsKey(toolStr))
                .map(toolStr -> (Tool) worldService.getItemMap().get(toolStr))
                .filter(tool1 -> tool1.getItemIndex() == 1)
                .findFirst()
                .orElse(new Tool());
        for (int i = 0; i < tool.getSkills().size() && i < SkillConstants.SKILL_LENGTH; i ++) {
            if (null == tool.getSkills().get(i)) {
                continue;
            }
            playerInfo.getSkills().set(i, new Skill(tool.getSkills().get(i)));
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public boolean validateActiveness(final GameWorld world, final String id) {
        if (!world.getCreatureMap().containsKey(id)) {
            return false;
        }
        Block block = world.getCreatureMap().get(id);
        if (block.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_PLAYER) {
            return false;
        }
        PlayerInfo playerInfo = block.getPlayerInfo();
        return playerInfo.getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING
                && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0
                && world.getOnlineMap().containsKey(block.getBlockInfo());
    }
}
