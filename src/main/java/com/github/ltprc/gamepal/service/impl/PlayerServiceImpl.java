package com.github.ltprc.gamepal.service.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.item.Consumable;
import com.github.ltprc.gamepal.model.item.Junk;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Integer RELATION_INIT = 0;
    private static final Integer RELATION_MIN = -100;
    private static final Integer RELATION_MAX = 100;
    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
    private Map<String, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> relationMap = new ConcurrentHashMap<>();
    private Set<String> flagSet = new ConcurrentHashSet<>();

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WorldService worldService;

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

    private void generateNotificationMessage(String userCode, String content) {
        Message message = new Message();
        message.setType(GamePalConstants.MESSAGE_TYPE_PRINTED);
        message.setScope(GamePalConstants.SCOPE_SELF);
        message.setToUserCode(userCode);
        message.setContent(content);
        messageService.sendMessage(userCode, message);
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
    public ResponseEntity useItem(String userCode, String itemNo, int itemAmount) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (!StringUtils.hasText(itemNo) || !playerInfo.getItems().containsKey(itemNo)
                || playerInfo.getItems().get(itemNo) == 0 || itemAmount <= 0) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1020));
        }
        switch (itemNo.charAt(0)) {
            case GamePalConstants.ITEM_CHARACTER_TOOL:
                if (playerInfo.getTools().contains(itemNo)) {
                    playerInfo.getTools().clear();
                } else {
                    playerInfo.getTools().clear();
                    playerInfo.getTools().add(itemNo);
                }
                break;
            case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                if (playerInfo.getOutfits().contains(itemNo)) {
                    playerInfo.getOutfits().clear();
                } else {
                    playerInfo.getOutfits().clear();
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
                ((Junk) worldService.getItemMap().get(itemNo)).getMaterials().entrySet().stream().forEach(entry -> {
                    getItem(userCode, entry.getKey(), entry.getValue());
                });
                break;
            case GamePalConstants.ITEM_CHARACTER_NOTE:
                break;
            case GamePalConstants.ITEM_CHARACTER_RECORDING:
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
            if (playerInfo.getTools().contains(itemNo)){
                playerInfo.getTools().clear();
            }
            if (playerInfo.getOutfits().contains(itemNo)){
                playerInfo.getOutfits().clear();
            }
        }
        BigDecimal capacity = playerInfo.getCapacity();
        playerInfo.setCapacity(capacity.add(worldService.getItemMap().get(itemNo).getWeight()
                .multiply(BigDecimal.valueOf(itemAmount))));
        if (itemAmount < 0) {
            generateNotificationMessage(userCode,
                    "失去 " + worldService.getItemMap().get(itemNo).getName() + " * " + (-1) * itemAmount);
        } else if (itemAmount > 0) {
            generateNotificationMessage(userCode,
                    "获得 " + worldService.getItemMap().get(itemNo).getName() + " * " + itemAmount);
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
                    "取出 " + worldService.getItemMap().get(itemNo).getName() + " * " + (-1) * itemAmount);
        } else if (itemAmount > 0) {
            generateNotificationMessage(userCode,
                    "存入 " + worldService.getItemMap().get(itemNo).getName() + " * " + itemAmount);
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
        // TODO Check death
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
                    .forEach(entry -> {
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
                        }
                    });
        }
        getItem(userCode, itemNo, -1 * itemAmount);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity interactBlocks(String userCode, int interactionCode, String id) {
        JSONObject rst = ContentUtil.generateRst();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        GameWorld world = userService.getWorldByUserCode(userCode);
        WorldBlock block = world.getBlockMap().get(id);
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
                        generateNotificationMessage(userCode, "你加入了桌游局。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_COOKER:
                        generateNotificationMessage(userCode, "你对于如何烹饪一无所知。");
                        break;
                    case GamePalConstants.BLOCK_TYPE_SINK:
                        generateNotificationMessage(userCode, "你清洗了一下双手。");
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
        }
        return ResponseEntity.ok().body(rst.toString());
    }
}
