package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.BuffConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.MessageConstants;
import com.github.ltprc.gamepal.config.MissionConstants;
import com.github.ltprc.gamepal.config.MovementConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.factory.BlockFactory;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.ItemManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.Message;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.MissionInfo;
import com.github.ltprc.gamepal.model.creature.NpcBrain;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.Skill;
import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.item.Junk;
import com.github.ltprc.gamepal.model.item.Tool;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.MessageService;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Log logger = LogFactory.getLog(PlayerServiceImpl.class);
    private static final Random random = new Random();

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WorldService worldService;

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

    @Autowired
    private ItemManager itemManager;

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
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        boolean hasChange = false;
        Integer playerStatus = req.getInteger("playerStatus");
        if (null != playerStatus && playerInfo.getPlayerStatus() != playerStatus) {
            playerInfo.setPlayerStatus(playerStatus);
            hasChange = true;
        }
        String firstName = req.getString("firstName");
        if (StringUtils.isNotBlank(firstName) && !StringUtils.equals(playerInfo.getFirstName(), firstName)) {
            playerInfo.setFirstName(firstName);
            hasChange = true;
        }
        String lastName = req.getString("lastName");
        if (StringUtils.isNotBlank(lastName) && !StringUtils.equals(playerInfo.getLastName(), lastName)) {
            playerInfo.setLastName(lastName);
            hasChange = true;
        }
        String nickname = req.getString("nickname");
        if (StringUtils.isNotBlank(nickname) && !StringUtils.equals(playerInfo.getNickname(), nickname)) {
            playerInfo.setNickname(nickname);
            hasChange = true;
        }
        String nameColor = req.getString("nameColor");
        if (StringUtils.isNotBlank(nameColor) && !StringUtils.equals(playerInfo.getNameColor(), nameColor)) {
            playerInfo.setNameColor(nameColor);
            hasChange = true;
        }
        Integer creatureType = req.getInteger("creatureType");
        if (null != creatureType && playerInfo.getCreatureType() != creatureType) {
            playerInfo.setCreatureType(creatureType);
            hasChange = true;
        }
        Integer gender = req.getInteger("gender");
        if (null != gender && playerInfo.getGender() != gender) {
            playerInfo.setGender(gender);
            hasChange = true;
        }
        Integer skinColor = req.getInteger("skinColor");
        if (null != skinColor && playerInfo.getSkinColor() != skinColor) {
            playerInfo.setSkinColor(skinColor);
            hasChange = true;
        }
        Integer breastType = req.getInteger("breastType");
        if (null != breastType && playerInfo.getBreastType() != breastType) {
            playerInfo.setBreastType(breastType);
            hasChange = true;
        }
        Integer accessories = req.getInteger("accessories");
        if (null != accessories && playerInfo.getAccessories() != accessories) {
            playerInfo.setAccessories(accessories);
            hasChange = true;
        }
        Integer hairstyle = req.getInteger("hairstyle");
        if (null != hairstyle && playerInfo.getHairstyle() != hairstyle) {
            playerInfo.setHairstyle(hairstyle);
            hasChange = true;
        }
        String hairColor = req.getString("hairColor");
        if (StringUtils.isNotBlank(hairColor) && !StringUtils.equals(playerInfo.getHairColor(), hairColor)) {
            playerInfo.setHairColor(hairColor);
            hasChange = true;
        }
        Integer eyes = req.getInteger("eyes");
        if (null != eyes && playerInfo.getEyes() != eyes) {
            playerInfo.setEyes(eyes);
            hasChange = true;
        }
        Integer nose = req.getInteger("nose");
        if (null != nose && playerInfo.getNose() != nose) {
            playerInfo.setNose(nose);
            hasChange = true;
        }
        Integer mouth = req.getInteger("mouth");
        if (null != mouth && playerInfo.getMouth() != mouth) {
            playerInfo.setMouth(mouth);
            hasChange = true;
        }
        Integer tongue = req.getInteger("tongue");
        if (null != tongue && playerInfo.getTongue() != tongue) {
            playerInfo.setTongue(tongue);
            hasChange = true;
        }
        Integer eyebrows = req.getInteger("eyebrows");
        if (null != eyebrows && playerInfo.getEyebrows() != eyebrows) {
            playerInfo.setEyebrows(eyebrows);
            hasChange = true;
        }
        Integer moustache = req.getInteger("moustache");
        if (null != moustache && playerInfo.getMoustache() != moustache) {
            playerInfo.setMoustache(moustache);
            hasChange = true;
        }
        Integer beard = req.getInteger("beard");
        if (null != beard && playerInfo.getBeard() != beard) {
            playerInfo.setBeard(beard);
            hasChange = true;
        }
        JSONArray faceCoefs = req.getJSONArray("faceCoefs");
        if (null != faceCoefs) {
            playerInfo.setFaceCoefs(new int[CreatureConstants.FACE_COEFS_LENGTH]);
            for (int i = 0; i < CreatureConstants.FACE_COEFS_LENGTH; i++) {
                if (playerInfo.getFaceCoefs()[i] != faceCoefs.getInteger(i)) {
                    playerInfo.getFaceCoefs()[i] = faceCoefs.getInteger(i);
                }
            }
            hasChange = true;
        }
        String avatar = req.getString("avatar");
        if (StringUtils.isNotBlank(avatar) && !StringUtils.equals(playerInfo.getAvatar(), avatar)) {
            playerInfo.setAvatar(avatar);
            hasChange = true;
        }
        if (hasChange) {
            long timestamp = System.currentTimeMillis();
            player.getBlockInfo().setTimeUpdated(timestamp);
        }
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
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        // Only human can receive message 24/09/30
        if (playerInfo.getPlayerType() != GamePalConstants.PLAYER_TYPE_HUMAN) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1039));
        }
        Message message = new Message();
        message.setType(MessageConstants.MESSAGE_TYPE_PRINTED);
        message.setScope(MessageConstants.SCOPE_SELF);
        message.setFromUserCode(userCode);
        message.setToUserCode(userCode);
        message.setContent(content);
        return messageService.collectMessage(userCode, message);
    }

    @Override
    public Map<String, Integer> getRelationMapByUserCode(String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016 + " userCode: " + userCode);
            return new ConcurrentHashMap<>();
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (GamePalConstants.PLAYER_TYPE_HUMAN != playerInfoMap.get(userCode).getPlayerType()
                && CreatureConstants.CREATURE_TYPE_HUMAN != playerInfoMap.get(userCode).getCreatureType()) {
            return new ConcurrentHashMap<>();
        }
        Map<String, Map<String, Integer>> relationMap = world.getRelationMap();
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<>());
        }
        return relationMap.get(userCode);
    }

    /**
     * Only human player has relation records
     * @param userCode
     * @param nextUserCode
     * @param newRelation
     * @param isAbsolute
     * @return :
     */
    @Override
    public ResponseEntity<String> setRelation(String userCode, String nextUserCode, int newRelation, boolean isAbsolute,
                                              boolean isNotified) {
        JSONObject rst = ContentUtil.generateRst();
        if (userService.getWorldByUserCode(userCode) != userService.getWorldByUserCode(nextUserCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1017));
        }
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        Map<String, Map<String, Integer>> relationMap = world.getRelationMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!creatureMap.containsKey(nextUserCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + userCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (!playerInfoMap.containsKey(nextUserCode)) {
            logger.error(ErrorUtil.ERROR_1007 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if ((GamePalConstants.PLAYER_TYPE_HUMAN != playerInfoMap.get(userCode).getPlayerType()
                && CreatureConstants.CREATURE_TYPE_HUMAN != playerInfoMap.get(userCode).getCreatureType())
                || (GamePalConstants.PLAYER_TYPE_HUMAN != playerInfoMap.get(nextUserCode).getPlayerType()
                && CreatureConstants.CREATURE_TYPE_HUMAN != playerInfoMap.get(nextUserCode).getCreatureType())) {
            logger.error(ErrorUtil.ERROR_1037 + "userCode: " + nextUserCode);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1037));
        }
        if (!relationMap.containsKey(userCode)) {
            relationMap.put(userCode, new ConcurrentHashMap<>());
        }
        if (!relationMap.containsKey(nextUserCode)) {
            relationMap.put(nextUserCode, new ConcurrentHashMap<>());
        }
        if (!relationMap.get(userCode).containsKey(nextUserCode)) {
            relationMap.get(userCode).put(nextUserCode, CreatureConstants.RELATION_INIT);
        }
        if (!relationMap.get(nextUserCode).containsKey(userCode)) {
            relationMap.get(nextUserCode).put(userCode, CreatureConstants.RELATION_INIT);
        }
        if (!isAbsolute) {
            newRelation += relationMap.get(userCode).get(nextUserCode);
        }
        newRelation = Math.min(CreatureConstants.RELATION_MAX, Math.max(CreatureConstants.RELATION_MIN, newRelation));
        if (newRelation != relationMap.get(userCode).get(nextUserCode)) {
            if (isNotified) {
                generateNotificationMessage(userCode, "你将对"
                        + playerInfoMap.get(nextUserCode).getNickname() + "的关系"
                        + (newRelation > relationMap.get(userCode).get(nextUserCode) ? "提高" : "降低")
                        + "为" + newRelation);
                generateNotificationMessage(nextUserCode, playerInfoMap.get(userCode).getNickname()
                        + "将对你的关系" + (newRelation > relationMap.get(userCode).get(nextUserCode) ? "提高" : "降低")
                        + "为" + newRelation);
            }
            relationMap.get(userCode).put(nextUserCode, newRelation);
            relationMap.get(nextUserCode).put(userCode, newRelation);
        }
        rst.put("userCode", userCode);
        rst.put("nextUserCode", nextUserCode);
        rst.put("relation", newRelation);
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
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
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
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
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
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
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
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        int oldPrecision = playerInfo.getPrecision();
        int newPrecision = isAbsolute ? value : oldPrecision + value;
        playerInfo.setPrecision(Math.max(0, Math.min(newPrecision, playerInfo.getPrecisionMax())));
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
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] != 0
                || playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1043));
        }
        if (null == playerInfo.getSkills() || playerInfo.getSkills().size() <= skillNo) {
            logger.error(ErrorUtil.ERROR_1028 + " skillNo: " + skillNo);
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1028));
        }
        BagInfo bagInfo = world.getBagInfoMap().get(userCode);
        String ammoCode = playerInfo.getSkills().get(skillNo).getAmmoCode();
        if (StringUtils.isNotBlank(ammoCode) && bagInfo.getItems().getOrDefault(ammoCode, 0) == 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1040));
        }
        if (SkillConstants.SKILL_CODE_SHOOT_THROW_JUNK == skillNo
                && !itemManager.peekRandomJunk(world, userCode).isPresent()) {
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
                        itemManager.getItem(world, userCode, ammoCode, -1, false);
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
                        itemManager.getItem(world, userCode, ammoCode, -1, false);
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
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        Block player = creatureMap.get(userCode);
        WorldCoordinate worldCoordinate = player.getWorldCoordinate();
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        double gaussianValue = random.nextGaussian();
        // 将生成的值转换成指定的均值和标准差
        BigDecimal direction = player.getMovementInfo().getFaceDirection();
        BigDecimal shakingAngle = BigDecimal.valueOf(gaussianValue * (playerInfo.getPrecisionMax() - playerInfo.getPrecision()) / playerInfo.getPrecisionMax());
        if (playerInfo.getSkills().get(skillNo).getSkillType() == SkillConstants.SKILL_TYPE_ATTACK) {
            changePrecision(userCode, -500, false);
        }
        switch (playerInfo.getSkills().get(skillNo).getSkillCode()) {
            case SkillConstants.SKILL_CODE_BLOCK:
                if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLOCKED] != -1) {
                    playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLOCKED] = BuffConstants.BUFF_DEFAULT_FRAME_BLOCKED;
                }
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_BLOCK, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_HEAL:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_HEAL, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_CHEER:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_CHEER, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_CURSE:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_CURSE, userCode, worldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_MELEE_HIT:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_HIT, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_KICK:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_KICK, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_SCRATCH:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_SCRATCH, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_SMASH:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_SMASH, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_CLEAVE:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_CLEAVE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_CHOP:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_CHOP, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_PICK:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_PICK, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_MELEE_STAB:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOCK, userCode, player.getWorldCoordinate());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_STAB, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_HIT:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_HIT, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_ARROW:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_ARROW, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_GUN:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_SLUG, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_SHOTGUN:
                for (int i = 0; i < 10; i++) {
                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_SLUG, userCode,
                            BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                    direction.add(shakingAngle)
                                            .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                            * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT_SHOTGUN));
                }
                break;
            case SkillConstants.SKILL_CODE_SHOOT_MAGNUM:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_MAGNUM, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_ROCKET:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_ROCKET, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_FIRE:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_FIRE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle)
                                        .add(BigDecimal.valueOf(SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                                        * 2 * (random.nextDouble() - 0.5D))), SkillConstants.SKILL_RANGE_SHOOT_FIRE_MAX));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_SPRAY:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_SPRAY, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_SHOOT_SPRAY));
                break;
            case SkillConstants.SKILL_CODE_SHOOT_THROW_JUNK:
                Optional<Junk> junk = itemManager.peekRandomJunk(world, userCode);
                if (junk.isPresent()) {
                    itemManager.getItem(world, userCode, junk.get().getItemNo(), -1, true);
                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SHOOT_THROW_JUNK, userCode,
                            BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                    direction.add(shakingAngle), SkillUtil.calculateThrowJunkDistance(junk.get())));
                } else {
                    return false;
                }
                break;
            case SkillConstants.SKILL_CODE_BUILD:
            case SkillConstants.SKILL_CODE_PLOW:
                Optional<BlockInfo> blockInfo1 = playerInfo.getTools().stream()
                        .map(BlockUtil::convertItemNo2BlockInfo)
                        .findFirst();
                if (!blockInfo1.isPresent()) {
                    return false;
                }
                int blockType, gridBlockCode;
                if (playerInfo.getSkills().get(skillNo).getSkillCode() == SkillConstants.SKILL_CODE_BUILD) {
                    blockType = BlockConstants.BLOCK_TYPE_BUILDING;
                    gridBlockCode = BlockConstants.BLOCK_CODE_SUBTERRANEAN;
                } else if (playerInfo.getSkills().get(skillNo).getSkillCode() == SkillConstants.SKILL_CODE_PLOW) {
                    blockType = BlockConstants.BLOCK_TYPE_FARM;
                    gridBlockCode = BlockConstants.BLOCK_CODE_ROUGH;
                } else {
                    return false;
                }
                WorldCoordinate buildingWorldCoordinate = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                        player.getWorldCoordinate(), direction, SkillConstants.SKILL_RANGE_BUILD);
                IntegerCoordinate integerCoordinate = BlockUtil.convertCoordinate2ClosestIntegerCoordinate(buildingWorldCoordinate);
                buildingWorldCoordinate.setCoordinate(new Coordinate(
                        BigDecimal.valueOf(integerCoordinate.getX()),
                        BigDecimal.valueOf(integerCoordinate.getY()),
                        player.getWorldCoordinate().getCoordinate().getZ()));
                Block fakeBuilding = new Block(buildingWorldCoordinate,
                        BlockFactory.createBlockInfoByCode(blockInfo1.get().getCode()),
                        new MovementInfo());
                if (!sceneManager.checkBlockSpace2Build(world, fakeBuilding)) {
                    return false;
                }
                sceneManager.setGridBlockCode(world, buildingWorldCoordinate, gridBlockCode);
                sceneManager.addOtherBlock(world, buildingWorldCoordinate, blockInfo1.get().getCode());
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_TAIL_SMOKE, userCode, buildingWorldCoordinate);
                break;
            case SkillConstants.SKILL_CODE_FISH:
                buildingWorldCoordinate = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                        player.getWorldCoordinate(), direction, SkillConstants.SKILL_RANGE_BUILD);
                boolean fishingResult = false;
                if (sceneManager.getGridBlockCode(world, buildingWorldCoordinate) == BlockConstants.BLOCK_CODE_WATER_SHALLOW
                        || sceneManager.getGridBlockCode(world, buildingWorldCoordinate) == BlockConstants.BLOCK_CODE_WATER_MEDIUM
                        || sceneManager.getGridBlockCode(world, buildingWorldCoordinate) == BlockConstants.BLOCK_CODE_WATER_DEEP) {
                    fishingResult = goFishing(userCode);
                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SPRAY, userCode, buildingWorldCoordinate);
                } else {
                    eventManager.addEvent(world, BlockConstants.BLOCK_CODE_LIGHT_SMOKE, userCode, buildingWorldCoordinate);
                }
                if (!fishingResult) {
                    generateNotificationMessage(userCode, "垂钓一无所获。");
                }
                break;
            case SkillConstants.SKILL_CODE_SHOVEL:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MELEE_SMASH, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                buildingWorldCoordinate = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                        player.getWorldCoordinate(), direction, SkillConstants.SKILL_RANGE_BUILD);
                if (sceneManager.getGridBlockCode(world, buildingWorldCoordinate) != BlockConstants.BLOCK_CODE_DIRT) {
                    sceneManager.setGridBlockCode(world, buildingWorldCoordinate, BlockConstants.BLOCK_CODE_DIRT);
                } else {
                    sceneManager.setGridBlockCode(world, buildingWorldCoordinate, BlockConstants.BLOCK_CODE_WATER_SHALLOW);
                }
                break;
            case SkillConstants.SKILL_CODE_DODGE:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_LIGHT_SMOKE, userCode, worldCoordinate);
                movementManager.speedUpBlock(world, player, BlockUtil.locateCoordinateWithDirectionAndDistance(
                        new Coordinate(), direction, BlockConstants.DODGE_RADIUS));
                player.getMovementInfo().setFaceDirection(direction);
                break;
            case SkillConstants.SKILL_CODE_LAY_MINE:
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_MINE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_LIGHT_SMOKE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_LAY_BARRIER:
                sceneManager.addOtherBlock(world,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE),
                        BlockConstants.BLOCK_CODE_ASH_PILE);
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_LIGHT_SMOKE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            case SkillConstants.SKILL_CODE_LAY_WIRE_NETTING:
//                blockInfo.getStructure().setShape(new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
//                        new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
//                        new Coordinate(BlockConstants.WIRE_NETTING_RADIUS, BlockConstants.WIRE_NETTING_RADIUS)));
                sceneManager.addOtherBlock(world,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE),
                        BlockConstants.BLOCK_CODE_WIRE_NETTING);
                eventManager.addEvent(world, BlockConstants.BLOCK_CODE_LIGHT_SMOKE, userCode,
                        BlockUtil.locateCoordinateWithDirectionAndDistance(region, player.getWorldCoordinate(),
                                direction.add(shakingAngle), SkillConstants.SKILL_RANGE_MELEE));
                break;
            default:
                break;
        }
        return true;
    }

    private boolean goFishing(final String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(String.valueOf(ErrorUtil.ERROR_1016));
            return false;
        }
        int randomValue = random.nextInt(100);
        if (randomValue < 5) {
            itemManager.getItem(world, userCode, "c035", 1, true);
        } else if (randomValue < 10) {
            itemManager.getItem(world, userCode, "m006", 1, true);
        } else if (randomValue < 12) {
            itemManager.getItem(world, userCode, "j082", 1, true);
        } else if (randomValue < 15) {
            itemManager.getItem(world, userCode, "j126", 1, true);
        } else if (randomValue < 18) {
            itemManager.getItem(world, userCode, "j135", 1, true);
        } else if (randomValue < 20) {
            itemManager.getItem(world, userCode, "j145", 1, true);
        } else if (randomValue < 22) {
            itemManager.getItem(world, userCode, "j191", 1, true);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public String findTopBossId(final String userCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            logger.error(String.valueOf(ErrorUtil.ERROR_1016));
            return userCode;
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            logger.error(String.valueOf(ErrorUtil.ERROR_1007));
            return userCode;
        }
        Block player = creatureMap.get(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            logger.error(String.valueOf(ErrorUtil.ERROR_1007));
            return userCode;
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        while (StringUtils.isNotBlank(playerInfo.getBossId())
                && !playerInfo.getBossId().equals(player.getBlockInfo().getId())) {
            player = creatureMap.get(playerInfo.getBossId());
        }
        return player.getBlockInfo().getId();
    }

    @Override
    public ResponseEntity<String> setMember(String userCode, String userCode1, String userCode2, boolean isNotified) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode1);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        if (userCode.equals(userCode1)) {
            if (StringUtils.isBlank(userCode2)) {
                if (isNotified) {
                    generateNotificationMessage(userCode, "你自立了，自此不为任何人效忠。");
                }
                playerInfoMap.get(userCode).setBossId(null);
                playerInfoMap.get(userCode).setTopBossId(findTopBossId(userCode));
                return ResponseEntity.ok().body(rst.toString());
            }
            if (!creatureMap.containsKey(userCode2)) {
                return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
            }
            String nextUserCodeBossId = userCode2;
            while (StringUtils.isNotBlank(nextUserCodeBossId)) {
                if (nextUserCodeBossId.equals(userCode)) {
                    if (isNotified) {
                        generateNotificationMessage(userCode, playerInfoMap.get(userCode2).getNickname()
                                + "是你的下级，你不可以为其效忠。");
                    }
                    return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
                }
                nextUserCodeBossId = playerInfoMap.get(nextUserCodeBossId).getBossId();
            }
            if (random.nextInt(CreatureConstants.RELATION_MAX) > getRelationMapByUserCode(userCode).getOrDefault(userCode2, 0)) {
                if (isNotified) {
                    generateNotificationMessage(userCode, playerInfoMap.get(userCode2).getNickname()
                            + "经过短暂思考，认为你不可以为其效忠。");
                }
                return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
            }
            if (isNotified) {
                generateNotificationMessage(userCode, "你向" + playerInfoMap.get(userCode2).getNickname()
                        + "屈从了，自此为其效忠。");
            }
            playerInfoMap.get(userCode).setBossId(userCode2);
            playerInfoMap.get(userCode).setTopBossId(findTopBossId(userCode));
        } else {
            PlayerInfo playerInfo1 = playerInfoMap.get(userCode1);
            if (!playerInfo1.getBossId().equals(userCode)) {
                if (isNotified) {
                    generateNotificationMessage(userCode, "你无法驱逐" + playerInfo1.getNickname()
                            + "，这不是你的直属下级。");
                }
                return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
            } else if (StringUtils.isNotBlank(userCode2)) {
                if (isNotified) {
                    generateNotificationMessage(userCode, "你无法指派你的直属下级" + playerInfo1.getNickname()
                            + "向他人效忠。");
                }
                return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1033));
            } else {
                playerInfo1.setBossId("");
                playerInfo1.setTopBossId(findTopBossId(userCode1));
                if (isNotified) {
                    generateNotificationMessage(userCode, "你驱逐了" + playerInfo1.getNickname()
                            + "，对你的效忠就此终止。");
                }
            }
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> addDrop(String userCode, String itemNo, int amount) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
//        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
//        if (!playerInfoMap.containsKey(userCode)) {
//            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
//        }
        Block player = creatureMap.get(userCode);
        Block drop = sceneManager.addDropBlock(world, player.getWorldCoordinate(),
                new AbstractMap.SimpleEntry<>(itemNo, amount));
        movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(new Coordinate(),
                BigDecimal.valueOf(random.nextDouble() * 360), BlockConstants.DROP_THROW_RADIUS));
//        MovementInfo movementInfo = block.getMovementInfo();
//        dropMovementInfo.setFaceDirection(BigDecimal.valueOf(random.nextDouble() * 360));
//        Coordinate newSpeed = BlockUtil.locateCoordinateWithDirectionAndDistance(new Coordinate(),
//                dropMovementInfo.getFaceDirection(), GamePalConstants.DROP_THROW_RADIUS);
//        dropMovementInfo.setSpeed(newSpeed);
//        movementManager.settleSpeedAndCoordinate(world, drop, 0);
//        dropMovementInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> useDrop(String userCode, String dropId) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        if (world.getDropMap().containsKey(dropId)) {
            Map.Entry<String, Integer> drop = world.getDropMap().get(dropId);
            itemManager.getItem(world, userCode, drop.getKey(), drop.getValue(), true);
        } else {
            logger.warn(String.valueOf(ErrorUtil.ERROR_1030));
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1030));
        }
        if (world.getBlockMap().containsKey(dropId)) {
            Block dropBlock = world.getBlockMap().get(dropId);
            sceneManager.removeBlock(world, dropBlock, false);
        } else {
            logger.warn(String.valueOf(ErrorUtil.ERROR_1030));
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1030));
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    @Transactional
    public ResponseEntity<String> knockPlayer(String userCode) {
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
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_INVINCIBLE] != 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1036));
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1043));
        }

        player.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        // Reset all skill remaining time
        for (int i = 0; i < playerInfo.getSkills().size(); i++) {
            if (null != playerInfo.getSkills().get(i)) {
                playerInfo.getSkills().get(i).setFrame(playerInfo.getSkills().get(i).getFrameMax());
            }
        }
        if (playerInfo.getPlayerType() != GamePalConstants.PLAYER_TYPE_HUMAN) {
            npcManager.resetNpcBrainQueues(userCode);
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_ONE_HIT] != 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] = 0;
        } else if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] == 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] = BuffConstants.BUFF_DEFAULT_FRAME_KNOCKED;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    @Transactional
    public ResponseEntity<String> killPlayer(String userCode) {
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
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        addPlayerTrophy(userCode, playerInfo.getBuff()[BuffConstants.BUFF_CODE_TROPHY] != 0);
        changeVp(userCode, 0, true);
        changeHunger(userCode, 0, true);
        changeThirst(userCode, 0, true);
        changePrecision(userCode, 0, true);
        eventManager.addEvent(world, BlockConstants.BLOCK_CODE_DECAY, userCode, player.getWorldCoordinate());
        buffManager.resetBuff(playerInfo);
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_REALISTIC] != 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] = -1;
            destroyPlayer(userCode);
        } else if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_REVIVED] != 0) {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] = BuffConstants.BUFF_DEFAULT_FRAME_DEAD;
        } else {
            playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] = -1;
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    @Transactional
    public ResponseEntity<String> pullPlayer(String fromUserCode, String toUserCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(toUserCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(toUserCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(toUserCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(toUserCode);
        PlayerInfo playerInfo = playerInfoMap.get(toUserCode);
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] = 0;
        eventManager.changeHp(world, player, BigDecimal.valueOf(player.getBlockInfo().getHpMax().get())
                .multiply(BlockConstants.HP_PULL_RATIO).intValue(), true);
        generateNotificationMessage(fromUserCode, "你将对方从濒死状态救助了。");
        generateNotificationMessage(toUserCode, "你从濒死状态被救助了。");
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    @Transactional
    public ResponseEntity<String> revivePlayer(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] = 0;
        playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] = 0;
        eventManager.changeHp(world, player, BigDecimal.valueOf(player.getBlockInfo().getHpMax().get())
                .multiply(BlockConstants.HP_RESPAWN_RATIO).intValue(), true);
        changeVp(userCode, playerInfo.getVpMax(), true);
        changeHunger(userCode, playerInfo.getHungerMax(), true);
        changeThirst(userCode, playerInfo.getThirstMax(), true);
        changePrecision(userCode, playerInfo.getPrecisionMax(), true);

        movementManager.settleCoordinate(world, player, playerInfo.getRespawnPoint(), true);

        eventManager.addEvent(world, BlockConstants.BLOCK_CODE_SACRIFICE, userCode, player.getWorldCoordinate());

        buffManager.resetBuff(playerInfo);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> addPlayerTrophy(String userCode, boolean hasTrophy) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        BagInfo bagInfo = world.getBagInfoMap().get(userCode);

        Block remainContainer = sceneManager.addOtherBlock(world, player.getWorldCoordinate(),
                playerInfo.getCreatureType() == CreatureConstants.CREATURE_TYPE_HUMAN
                        ? BlockConstants.BLOCK_CODE_HUMAN_REMAIN_DEFAULT
                        : BlockConstants.BLOCK_CODE_ANIMAL_REMAIN_DEFAULT);
        String remainId = remainContainer.getBlockInfo().getId();
        if (playerInfo.getCreatureType() == CreatureConstants.CREATURE_TYPE_HUMAN) {
            PlayerInfo remainPlayerInfo = CreatureFactory.createCreatureInstance(playerInfo.getPlayerType(),
                    playerInfo.getCreatureType());
            CreatureFactory.copyPersonalizedPlayerInfo(playerInfo, remainPlayerInfo);
            remainPlayerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] = -1;
            CreatureFactory.copyDisplayedPlayerInfo(playerInfo, remainPlayerInfo);
            playerInfoMap.put(remainId, remainPlayerInfo);
        }

        movementManager.speedUpBlock(world, remainContainer, BlockUtil.locateCoordinateWithDirectionAndDistance(
                new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                BlockConstants.REMAIN_CONTAINER_THROW_RADIUS));

        if (hasTrophy) {
            Map<String, Integer> itemsMap = new HashMap<>(bagInfo.getItems());
            itemsMap.forEach((key, value) -> {
                itemManager.getItem(world, userCode, key, -value, false);
                itemManager.getItem(world, remainId, key, value, false);
            });
        }
        switch (playerInfo.getCreatureType()) {
            case CreatureConstants.CREATURE_TYPE_HUMAN:
                break;
            case CreatureConstants.CREATURE_TYPE_ANIMAL:
                switch (playerInfo.getSkinColor()) {
                    case CreatureConstants.SKIN_COLOR_PAOFU:
                    case CreatureConstants.SKIN_COLOR_CAT:
                        itemManager.getItem(world, remainId, "c038", random.nextInt(2) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_FROG:
                    case CreatureConstants.SKIN_COLOR_MONKEY:
                    case CreatureConstants.SKIN_COLOR_RACOON:
                        itemManager.getItem(world, remainId, "m004", 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_CHICKEN:
                        if (random.nextDouble() < 0.25D) {
                            itemManager.getItem(world, remainId, "c040", random.nextInt(1) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c031", random.nextInt(2) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_BUFFALO:
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "j037", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.4D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(3) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c032", random.nextInt(4) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_FOX:
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(2) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c037", random.nextInt(2) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_POLAR_BEAR:
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "j191", random.nextInt(4) + 1, false);
                        }
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(2) + 1, false);
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_SHEEP:
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c034", random.nextInt(2) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_TIGER:
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "j191", random.nextInt(4) + 1, false);
                        }
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(3) + 1, false);
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_DOG:
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "j063", 1, false);
                        }
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "j193", 1, false);
                        }
                        if (random.nextDouble() < 0.25D) {
                            itemManager.getItem(world, remainId, "j191", random.nextInt(4) + 1, false);
                        }
                        if (random.nextDouble() < 0.2D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(2) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c037", random.nextInt(2) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_WOLF:
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "j191", random.nextInt(4) + 1, false);
                        }
                        if (random.nextDouble() < 0.2D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.2D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(2) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c037", random.nextInt(3) + 1, false);
                        break;
                    case CreatureConstants.SKIN_COLOR_BOAR:
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "j191", random.nextInt(4) + 1, false);
                        }
                        if (random.nextDouble() < 0.4D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.5D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(3) + 1, false);
                        }
                        break;
                    case CreatureConstants.SKIN_COLOR_HORSE:
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m006", random.nextInt(2) + 1, false);
                        }
                        if (random.nextDouble() < 0.1D) {
                            itemManager.getItem(world, remainId, "m004", random.nextInt(2) + 1, false);
                        }
                        itemManager.getItem(world, remainId, "c036", random.nextInt(2) + 1, false);
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
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (GamePalConstants.PLAYER_TYPE_HUMAN != playerInfo.getPlayerType()) {
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
            preservedBagInfo.setCapacityMax(BigDecimal.valueOf(CreatureConstants.CAPACITY_MAX));
        }
        player.getMovementInfo().setFaceDirection(MovementConstants.FACE_DIRECTION_DEFAULT);
        playerInfoMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(userCode))
                .filter(entry -> StringUtils.isNotBlank(entry.getValue().getBossId()))
                .filter(entry -> entry.getValue().getBossId().equals(userCode))
                .forEach(entry -> setMember(entry.getKey(), entry.getKey(), "", false));
        // TODO Game-over display
        userService.logoff(userCode, "", false);
        world.getCreatureMap().remove(userCode);
        world.getPlayerInfoMap().remove(userCode);
        if (GamePalConstants.PLAYER_TYPE_HUMAN != playerInfo.getPlayerType()) {
            world.getNpcBrainMap().remove(userCode);
        }
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> addExp(String userCode, int expVal) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        playerInfo.setExp(playerInfo.getExp() + expVal);
        checkLevelUp(world, userCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    private void checkLevelUp(GameWorld world, String userCode) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        if (playerInfo.getExp() >= playerInfo.getExpMax()) {
            playerInfo.setExp(0);
            playerInfo.setLevel(playerInfo.getLevel() + 1);
            SkillUtil.updateExpMax(playerInfo);
            eventManager.addEvent(world, BlockConstants.BLOCK_CODE_UPGRADE, userCode, player.getWorldCoordinate());
        }
    }

    @Override
    public ResponseEntity<String> updateSkillsByTool(String userCode) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        if (!playerInfoMap.containsKey(userCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1007));
        }
        Block player = creatureMap.get(userCode);
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        if (!creatureMap.containsKey(id)) {
            logger.warn(String.valueOf(ErrorUtil.ERROR_1007));
            return false;
        }
        Block block = creatureMap.get(id);
        if (block.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_PLAYER) {
            return false;
        }
        if (!world.getCreatureMap().containsKey(id) || !world.getPlayerInfoMap().containsKey(id)
                || !world.getOnlineMap().containsKey(id)) {
            return false;
        }
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(id);
        return playerInfo.getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING
                && playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] == 0;
    }

    @Override
    public String toReadableString(Block player, PlayerInfo playerInfo) {
        JSONObject rst = toReadableJson(playerInfo);
        rst.put("ID", player.getBlockInfo().getId());
        rst.put("生命值", player.getBlockInfo().getHp() + "/" + player.getBlockInfo().getHpMax());
        String str = JSON.toJSONString(rst.toString());
        return str.substring(1, str.length() - 1);
    }

    @Override
    public String toReadableString(PlayerInfo playerInfo) {
        String str = JSON.toJSONString(toReadableJson(playerInfo).toString());
        return str.substring(1, str.length() - 1);
    }

    private JSONObject toReadableJson(PlayerInfo playerInfo) {
        JSONObject rst = new JSONObject();
        switch (playerInfo.getPlayerType()) {
            case GamePalConstants.PLAYER_TYPE_HUMAN:
                rst.put("玩家类型", "真人");
                break;
            case GamePalConstants.PLAYER_TYPE_NPC:
                rst.put("玩家类型", "人工智能");
                break;
        }
        switch (playerInfo.getPlayerStatus()) {
            case GamePalConstants.PLAYER_STATUS_INIT:
                rst.put("玩家状态", "未初始化");
                break;
            case GamePalConstants.PLAYER_STATUS_RUNNING:
                rst.put("玩家类型", "正常");
                break;
        }
        // TODO 头像
        rst.put("名", playerInfo.getFirstName());
        rst.put("姓", playerInfo.getLastName());
        rst.put("昵称", playerInfo.getNickname());
        rst.put("姓名背景色字符串", playerInfo.getNameColor());
        switch (playerInfo.getCreatureType()) {
            case CreatureConstants.CREATURE_TYPE_HUMAN:
                rst.put("生物类型", "人类");
                rst.put("肤色（从0到100代表白种-亚裔-拉丁-非裔）", playerInfo.getSkinColor());
                break;
            case CreatureConstants.CREATURE_TYPE_ANIMAL:
                switch (playerInfo.getSkinColor()) {
                    case 1:
                        rst.put("生物类型", "英短和折耳混血猫-香香软软的小泡芙");
                        break;
                    case 2:
                        rst.put("生物类型", "青蛙");
                        break;
                    case 3:
                        rst.put("生物类型", "猴子");
                        break;
                    case 4:
                        rst.put("生物类型", "浣熊");
                        break;
                    case 5:
                        rst.put("生物类型", "鸡");
                        break;
                    case 6:
                        rst.put("生物类型", "牛");
                        break;
                    case 7:
                        rst.put("生物类型", "狐狸");
                        break;
                    case 8:
                        rst.put("生物类型", "北极熊");
                        break;
                    case 9:
                        rst.put("生物类型", "绵羊");
                        break;
                    case 10:
                        rst.put("生物类型", "老虎");
                        break;
                    case 11:
                        rst.put("生物类型", "猫");
                        break;
                    case 12:
                        rst.put("生物类型", "狗");
                        break;
                    case 13:
                        rst.put("生物类型", "狼");
                        break;
                    case 14:
                        rst.put("生物类型", "野猪");
                        break;
                    case 15:
                        rst.put("生物类型", "马");
                        break;
                    default:
                        rst.put("生物类型", "动物");
                        break;
                }
                break;
        }
        switch (playerInfo.getGender()) {
            case CreatureConstants.GENDER_MALE:
                rst.put("性别", "男");
                break;
            case CreatureConstants.GENDER_FEMALE:
                rst.put("性别", "女");
                break;
        }
        // TODO 人体细节参数
        String buffStr = "";
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] != 0) {
            buffStr += "死亡 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_STUNNED] != 0) {
            buffStr += "昏迷 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLEEDING] != 0) {
            buffStr += "流血 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_SICK] != 0) {
            buffStr += "疾病 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_FRACTURED] != 0) {
            buffStr += "骨折 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_HUNGRY] != 0) {
            buffStr += "饥饿 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_THIRSTY] != 0) {
            buffStr += "口渴 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_FATIGUED] != 0) {
            buffStr += "疲惫 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLIND] != 0) {
            buffStr += "失明 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_INVINCIBLE] != 0) {
            buffStr += "无敌 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_ONE_HIT] != 0) {
            buffStr += "一击 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_REALISTIC] != 0) {
            buffStr += "写实 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_TROPHY] != 0) {
            buffStr += "掉落 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_BLOCKED] != 0) {
            buffStr += "格挡 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_HAPPY] != 0) {
            buffStr += "愉悦 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_SAD] != 0) {
            buffStr += "沮丧 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_RECOVERING] != 0) {
            buffStr += "康复 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_OVERWEIGHTED] != 0) {
            buffStr += "超重 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_KNOCKED] != 0) {
            buffStr += "濒死 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_REVIVED] != 0) {
            buffStr += "急救 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DIVING] != 0) {
            buffStr += "潜水 ";
        }
        if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_DROWNING] != 0) {
            buffStr += "溺水 ";
        }
        rst.put("buff", buffStr);
        // TODO skills, perception
        rst.put("活力值", playerInfo.getVp() + "/" + playerInfo.getVpMax());
        rst.put("饮食值", playerInfo.getHunger() + "/" + playerInfo.getHungerMax());
        rst.put("饮水值", playerInfo.getThirst() + "/" + playerInfo.getThirstMax());
        rst.put("精准度", playerInfo.getPrecision() + "/" + playerInfo.getPrecisionMax());
        rst.put("等级", playerInfo.getLevel());
        rst.put("经验", playerInfo.getExp() + "/" + playerInfo.getExpMax());
        rst.put("金钱", playerInfo.getMoney());
        StringBuilder toolsStr = new StringBuilder();
        for (String toolCode : playerInfo.getTools()) {
            Item tool = worldService.getItemMap().get(toolCode);
            toolsStr.append(tool.getName());
            toolsStr.append(" ");
        }
        rst.put("工具", toolsStr.toString());
        StringBuilder outfitsStr = new StringBuilder();
        for (String outfitCode : playerInfo.getOutfits()) {
            Item outfit = worldService.getItemMap().get(outfitCode);
            outfitsStr.append(outfit.getName());
            outfitsStr.append(" ");
        }
        rst.put("服装", outfitsStr.toString());
        Optional.ofNullable(playerInfo.getBossId())
                .filter(StringUtils::isNotBlank)
                .map(bossId -> userService.getWorldByUserCode(bossId))
                .map(world -> world.getPlayerInfoMap().get(playerInfo.getBossId()))
                .map(PlayerInfo::getNickname)
                .ifPresent(bossNickName -> {
                    rst.put("直属上级ID", playerInfo.getBossId());
                    rst.put("直属上级昵称", bossNickName);
                });
        Optional.ofNullable(playerInfo.getTopBossId())
                .filter(StringUtils::isNotBlank)
                .map(topBossId -> userService.getWorldByUserCode(topBossId))
                .map(world -> world.getPlayerInfoMap().get(playerInfo.getTopBossId()))
                .map(PlayerInfo::getNickname)
                .ifPresent(topBossNickName -> {
                    rst.put("最高上级ID", playerInfo.getTopBossId());
                    rst.put("最高上级昵称", topBossNickName);
                });
        StringBuilder missionStr = new StringBuilder();
        for (MissionInfo missionInfo : playerInfo.getMissions()) {
            switch (missionInfo.getStatus()) {
                case MissionConstants.MISSION_STATUS_INITIATED:
                    missionStr.append("[进行中]");
                    break;
                case MissionConstants.MISSION_STATUS_COMPLETED:
                    missionStr.append("[已完成]");
                    break;
            }
            missionStr.append(missionInfo.getContent());
            missionStr.append(" ");
        }
        rst.put("任务", missionStr.toString());
        return rst;
    }
}
