package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.Skill;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.creature.NpcBrain;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class NpcManagerImpl implements NpcManager {

    private static final Log logger = LogFactory.getLog(NpcManagerImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CreatureFactory creatureFactory;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BuffManager buffManager;

    @Autowired
    private WorldService worldService;

    @Override
    public PlayerInfo createCreature(GameWorld world, final int playerType, final int creatureType, String userCode) {
        PlayerInfo playerInfo = creatureFactory.createCreatureInstance(playerType);
        playerInfo.setCreatureType(creatureType);
        playerInfo.setId(userCode);
        playerInfo.setFaceDirection(BigDecimal.valueOf(Math.random() * 360D));
        playerInfo.setTopBossId(userCode);
        BlockUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), world.getWorldTime());
        world.getPlayerInfoMap().put(userCode, playerInfo);
        buffManager.initializeBuff(playerInfo);
        BagInfo bagInfo = new BagInfo();
        bagInfo.setId(userCode);
        world.getBagInfoMap().put(userCode, bagInfo);
        bagInfo = new BagInfo();
        bagInfo.setId(userCode);
        world.getPreservedBagInfoMap().put(userCode, bagInfo);
        return playerInfo;
    }

    @Override
    public PlayerInfo putCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate) {
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo creatureInfo = playerInfoMap.get(userCode);
        BlockUtil.copyWorldCoordinate(worldCoordinate, creatureInfo);
        BlockUtil.fixWorldCoordinate(world.getRegionMap().get(worldCoordinate.getRegionNo()), creatureInfo);
        worldService.expandByCoordinate(world, null, creatureInfo,
                creatureInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN ? 1 : 0);
        world.getOnlineMap().put(userCode, -1L);
        world.getFlagMap().putIfAbsent(userCode, new boolean[FlagConstants.FLAG_LENGTH]);
        userService.addUserIntoWorldMap(userCode, world.getId());
        if (CreatureConstants.PLAYER_TYPE_HUMAN != creatureInfo.getPlayerType()) {
            NpcBrain npcBrain = generateNpcBrain();
            world.getNpcBrainMap().put(userCode, npcBrain);
            JSONObject behaviorRequest = new JSONObject();
            behaviorRequest.put("userCode", userCode);
            behaviorRequest.put("targetUserCode", userCode);
            behaviorRequest.put("behavior", CreatureConstants.NPC_BEHAVIOR_IDLE);
            JSONArray exemptionJsonArray = new JSONArray();
            if (CreatureConstants.CREATURE_TYPE_HUMAN == creatureInfo.getCreatureType()) {
                behaviorRequest.put("stance", CreatureConstants.STANCE_AGGRESSIVE);
                exemptionJsonArray.add(false);
                exemptionJsonArray.add(true);
                exemptionJsonArray.add(true);
                exemptionJsonArray.add(false);
            } else {
                switch (creatureInfo.getSkinColor()) {
                    case CreatureConstants.SKIN_COLOR_PAOFU:
                    case CreatureConstants.SKIN_COLOR_MONKEY:
                    case CreatureConstants.SKIN_COLOR_FOX:
                    case CreatureConstants.SKIN_COLOR_CAT:
                    case CreatureConstants.SKIN_COLOR_DOG:
                        behaviorRequest.put("stance", CreatureConstants.STANCE_DEFENSIVE);
                        break;
                    case CreatureConstants.SKIN_COLOR_RACOON:
                        behaviorRequest.put("stance", CreatureConstants.STANCE_STAND_GROUND);
                        break;
                    case CreatureConstants.SKIN_COLOR_FROG:
                    case CreatureConstants.SKIN_COLOR_CHICKEN:
                    case CreatureConstants.SKIN_COLOR_SHEEP:
                    case CreatureConstants.SKIN_COLOR_HORSE:
                        behaviorRequest.put("stance", CreatureConstants.STANCE_NO_ATTACK);
                        break;
                    case CreatureConstants.SKIN_COLOR_POLAR_BEAR:
                    case CreatureConstants.SKIN_COLOR_TIGER:
                    case CreatureConstants.SKIN_COLOR_WOLF:
                    case CreatureConstants.SKIN_COLOR_BOAR:
                    default:
                        behaviorRequest.put("stance", CreatureConstants.STANCE_AGGRESSIVE);
                        break;
                }
                switch (creatureInfo.getSkinColor()) {
                    case CreatureConstants.SKIN_COLOR_SHEEP:
                    case CreatureConstants.SKIN_COLOR_HORSE:
                    case CreatureConstants.SKIN_COLOR_FROG:
                    case CreatureConstants.SKIN_COLOR_CHICKEN:
                        exemptionJsonArray.add(true);
                        exemptionJsonArray.add(true);
                        break;
                    case CreatureConstants.SKIN_COLOR_PAOFU:
                    case CreatureConstants.SKIN_COLOR_MONKEY:
                    case CreatureConstants.SKIN_COLOR_FOX:
                    case CreatureConstants.SKIN_COLOR_CAT:
                    case CreatureConstants.SKIN_COLOR_DOG:
                    case CreatureConstants.SKIN_COLOR_RACOON:
                    case CreatureConstants.SKIN_COLOR_BUFFALO:
                        exemptionJsonArray.add(false);
                        exemptionJsonArray.add(true);
                        break;
                    case CreatureConstants.SKIN_COLOR_POLAR_BEAR:
                    case CreatureConstants.SKIN_COLOR_TIGER:
                    case CreatureConstants.SKIN_COLOR_WOLF:
                    case CreatureConstants.SKIN_COLOR_BOAR:
                    default:
                        exemptionJsonArray.add(false);
                        exemptionJsonArray.add(false);
                        break;
                }
                exemptionJsonArray.add(false);
                exemptionJsonArray.add(true);
            }
            behaviorRequest.put("exemption", exemptionJsonArray);
            changeNpcBehavior(behaviorRequest);
        }
        return creatureInfo;
    }

    private NpcBrain generateNpcBrain() {
        NpcBrain npcBrain = new NpcBrain();
        npcBrain.setExemption(new boolean[]{false, true, true, false}); // Default for all NPC including human and animals
        npcBrain.setBehavior(CreatureConstants.NPC_BEHAVIOR_IDLE);
        npcBrain.setStance(CreatureConstants.STANCE_AGGRESSIVE);
        npcBrain.setGreenQueue(new LinkedList<>());
        npcBrain.setYellowQueue(new LinkedList<>());
        npcBrain.setRedQueue(new LinkedList<>());
        return npcBrain;
    }

    @Override
    public JSONObject changeNpcBehavior(JSONObject request) {
        JSONObject rst = ContentUtil.generateRst();
        String npcUserCode = request.getString("userCode");
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        if (null == world) {
            logger.warn(ErrorUtil.ERROR_1016);
            return rst;
        }
        PlayerInfo npcPlayerInfo = world.getPlayerInfoMap().get(npcUserCode);
        Map<String, NpcBrain> npcBrainMap = world.getNpcBrainMap();
        NpcBrain npcBrain = npcBrainMap.get(npcUserCode);
        resetNpcBrainQueues(npcUserCode);
        String targetUserCode = request.getString("targetUserCode");
        WorldCoordinate targetWorldCoordinate = request.getObject("targetWorldCoordinate", WorldCoordinate.class);
        int behavior = request.getInteger("behavior");
        npcBrain.setBehavior(behavior);
        switch (behavior) {
            case CreatureConstants.NPC_BEHAVIOR_IDLE:
                npcBrain.getGreenQueue().clear();
                npcBrain.getYellowQueue().clear();
                npcBrain.getRedQueue().clear();
                break;
            case CreatureConstants.NPC_BEHAVIOR_MOVE:
                npcBrain.getGreenQueue().add(targetWorldCoordinate);
                break;
            case CreatureConstants.NPC_BEHAVIOR_PATROL:
                npcBrain.getGreenQueue().add(targetWorldCoordinate);
                npcBrain.getGreenQueue().add(new WorldCoordinate(npcPlayerInfo));
                break;
            case CreatureConstants.NPC_BEHAVIOR_FOLLOW:
                npcBrain.getGreenQueue().add(world.getPlayerInfoMap().get(targetUserCode));
                break;
            default:
                break;
        }
        npcBrain.setStance(request.getInteger("stance"));
        npcBrain.setExemption(new boolean[CreatureConstants.NPC_EXEMPTION_LENGTH]);
        JSONArray exemption = request.getJSONArray("exemption");
        for (int i = 0; i < CreatureConstants.NPC_EXEMPTION_LENGTH; i++) {
            npcBrain.getExemption()[i] = (boolean) exemption.get(i);
        }
        return rst;
    }

    @Override
    public PlayerInfo putSpecificCreature(GameWorld world, String userCode, WorldCoordinate worldCoordinate,
                                          final BigDecimal distance, final int playerType, final int creatureType,
                                          final int behavior, final int stance, final boolean[] exemption) {
        String npcUserCode = UUID.randomUUID().toString();
        PlayerInfo playerInfo = createCreature(world, playerType, creatureType, npcUserCode);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
        worldCoordinate.getCoordinate().setX(worldCoordinate.getCoordinate().getX()
                .add(distance.multiply(BigDecimal.valueOf(
                        1 * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI)))));
        worldCoordinate.getCoordinate().setY(worldCoordinate.getCoordinate().getY()
                .subtract(distance.multiply(BigDecimal.valueOf(
                        1 * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI)))));
        putCreature(world, npcUserCode, worldCoordinate);
        JSONObject behaviorRequest = new JSONObject();
        behaviorRequest.put("userCode", npcUserCode);
        behaviorRequest.put("targetUserCode", userCode);
        behaviorRequest.put("behavior", behavior);
        behaviorRequest.put("stance", stance);
        JSONArray exemptionJsonArray = new JSONArray();
        for (int i = 0; i < CreatureConstants.NPC_EXEMPTION_LENGTH; i++) {
            exemptionJsonArray.add(exemption[i]);
        }
        behaviorRequest.put("exemption", exemptionJsonArray);
        changeNpcBehavior(behaviorRequest);
        return playerInfo;
    }

    @Override
    public PlayerInfo putSpecificCreatureByRole(GameWorld world, String userCode, WorldCoordinate worldCoordinate,
                                                int role) {
        PlayerInfo npcPlayerInfo;
        switch (role) {
            case CreatureConstants.NPC_ROLE_PEER:
                npcPlayerInfo = putSpecificCreature(world, userCode, worldCoordinate, BigDecimal.ONE,
                        CreatureConstants.PLAYER_TYPE_NPC, CreatureConstants.CREATURE_TYPE_HUMAN,
                        CreatureConstants.NPC_BEHAVIOR_MOVE, CreatureConstants.STANCE_DEFENSIVE,
                        new boolean[]{false, false, true, false});
                if (StringUtils.isBlank(world.getPlayerInfoMap().get(userCode).getBossId())) {
                    playerService.setMember(npcPlayerInfo.getId(), npcPlayerInfo.getId(), userCode);
                } else {
                    playerService.setMember(npcPlayerInfo.getId(), npcPlayerInfo.getId(),
                            world.getPlayerInfoMap().get(userCode).getBossId());
                }
                break;
            case CreatureConstants.NPC_ROLE_MINION:
                npcPlayerInfo = putSpecificCreature(world, userCode, worldCoordinate, BigDecimal.ONE,
                        CreatureConstants.PLAYER_TYPE_NPC, CreatureConstants.CREATURE_TYPE_HUMAN,
                        CreatureConstants.NPC_BEHAVIOR_FOLLOW, CreatureConstants.STANCE_DEFENSIVE,
                        new boolean[]{false, false, true, false});
                playerService.setMember(npcPlayerInfo.getId(), npcPlayerInfo.getId(), userCode);
                break;
            default:
            case CreatureConstants.NPC_ROLE_INDIVIDUAL:
                npcPlayerInfo = putSpecificCreature(world, userCode, worldCoordinate, BigDecimal.ONE,
                        CreatureConstants.PLAYER_TYPE_NPC, CreatureConstants.CREATURE_TYPE_HUMAN,
                        CreatureConstants.NPC_BEHAVIOR_IDLE, CreatureConstants.STANCE_DEFENSIVE,
                        new boolean[]{false, false, true, false});
                break;
        }
        return npcPlayerInfo;
    }

    @Override
    public void updateNpcBrains(GameWorld world) {
        Map<String, NpcBrain> npcBrainMap = world.getNpcBrainMap();
        npcBrainMap.entrySet().stream()
                // Filtered NPCs not detected by human players 24/08/01
                .filter(entry2 -> world.getPlayerInfoMap().entrySet().stream()
                        .filter(entry3 -> CreatureConstants.PLAYER_TYPE_HUMAN == entry3.getValue().getPlayerType())
                        .anyMatch(entry3 -> SkillUtil.isBlockDetected(entry3.getValue(),
                                world.getPlayerInfoMap().get(entry3.getKey()), 1)))
                .filter(entry2 -> playerService.validateActiveness(world, world.getPlayerInfoMap().get(entry2.getKey())))
                .forEach(entry2 -> {
                    String npcUserCode = entry2.getKey();
                    PlayerInfo npcPlayerInfo = world.getPlayerInfoMap().get(npcUserCode);
                    NpcBrain npcBrain = entry2.getValue();
                    // Old targets
                    WorldCoordinate oldWc = npcBrain.getGreenQueue().peek();
                    if (null != oldWc) {
                        BigDecimal distance = BlockUtil.calculateDistance(
                                world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, oldWc);
                        if (npcBrain.getBehavior() == CreatureConstants.NPC_BEHAVIOR_PATROL && null != distance
                                && distance.compareTo(CreatureConstants.NPC_ARRIVE_DISTANCE) <= 0) {
                            npcBrain.getGreenQueue().add(npcBrain.getGreenQueue().poll());
                        }
                    }
                    oldWc = npcBrain.getYellowQueue().peek();
                    if (null != oldWc) {
                        BigDecimal distance = BlockUtil.calculateDistance(
                                world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, oldWc);
                        if (null == distance || distance.compareTo(CreatureConstants.NPC_ARRIVE_DISTANCE) <= 0) {
                            npcBrain.getYellowQueue().pop();
                        }
                    }
                    Optional<PlayerInfo> red = world.getPlayerInfoMap().values().stream()
                            .filter(playerInfo -> !npcUserCode.equals(playerInfo.getId()))
                            .filter(playerInfo -> playerService.validateActiveness(world, playerInfo))
                            .filter(playerInfo -> BlockUtil.checkPerceptionCondition(
                                    world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, playerInfo))
                            .min((playerInfo1, playerInfo2) -> {
                                BigDecimal distance1 = BlockUtil.calculateDistance
                                        (world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo,
                                                playerInfo1);
                                BigDecimal distance2 = BlockUtil.calculateDistance
                                        (world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo,
                                                playerInfo2);
                                assert distance1 != null;
                                return distance1.compareTo(distance2);
                            });
                    // Innocent exemption check must be here, otherwise self-defense would not work 24/08/08
                    if (red.isPresent() && !npcBrain.getExemption()[CreatureConstants.NPC_EXEMPTION_INNOCENT]) {
                        prepare2Attack(world, npcUserCode, red.get().getId());
                    }
                    // Remove not alive element from red queue 24/08/08
                    while (!npcBrain.getRedQueue().isEmpty()
                            && !playerService.validateActiveness(world, npcBrain.getRedQueue().peek())) {
                        npcBrain.getRedQueue().poll();
                    }
                    // Move & Destroy
                    if (!npcBrain.getRedQueue().isEmpty()
                            && SkillUtil.checkSkillTypeAttack(world.getPlayerInfoMap().get(npcUserCode))) {
                        hunt(world, npcUserCode);
                    } else if (!npcBrain.getYellowQueue().isEmpty()) {
                        JSONObject moveReq = new JSONObject();
                        moveReq.put("userCode", npcUserCode);
                        moveReq.put("wc", npcBrain.getYellowQueue().peek());
                        moveReq.put("stopDistance", CreatureConstants.NPC_ARRIVE_DISTANCE);
                        JSONObject moveResp = runNpcMoveTask(moveReq);
                    } else if (!npcBrain.getGreenQueue().isEmpty()) {
                        JSONObject moveReq = new JSONObject();
                        moveReq.put("userCode", npcUserCode);
                        moveReq.put("wc", npcBrain.getGreenQueue().peek());
                        moveReq.put("stopDistance", npcBrain.getBehavior() == CreatureConstants.NPC_BEHAVIOR_FOLLOW
                                ? CreatureConstants.NPC_FOLLOW_STOP_DISTANCE : CreatureConstants.NPC_ARRIVE_DISTANCE);
                        JSONObject moveResp = runNpcMoveTask(moveReq);
                    }
                });
        // Settle NPC speed
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        playerInfoMap.entrySet().stream()
                .filter(entry2 -> entry2.getValue().getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN)
                .filter(entry2 -> playerService.validateActiveness(world, entry2.getValue()))
                .forEach(entry2 -> movementManager.settleSpeedAndCoordinate(world, entry2.getValue(), 1));
    }

    private boolean checkAttackCondition(final String fromUserCode, final String toUserCode) {
        GameWorld world = userService.getWorldByUserCode(fromUserCode);
        if (null == world) {
            logger.warn(ErrorUtil.ERROR_1016);
            return false;
        }
        if (world != userService.getWorldByUserCode(toUserCode)) {
            return false;
        }
        if (world.getNpcBrainMap().get(fromUserCode).getStance() == CreatureConstants.STANCE_NO_ATTACK) {
            return false;
        }
        if (!SkillUtil.checkSkillTypeAttack(world.getPlayerInfoMap().get(fromUserCode))) {
            return false;
        }
        if (world.getNpcBrainMap().get(fromUserCode).getExemption()[CreatureConstants.NPC_EXEMPTION_TEAMMATE]
                && StringUtils.equals(world.getPlayerInfoMap().get(fromUserCode).getTopBossId(),
                world.getPlayerInfoMap().get(toUserCode).getTopBossId())) {
            return false;
        } else if (world.getNpcBrainMap().get(fromUserCode).getExemption()[CreatureConstants.NPC_EXEMPTION_SAME_CREATURE]
                && world.getPlayerInfoMap().get(fromUserCode).getCreatureType()
                == world.getPlayerInfoMap().get(toUserCode).getCreatureType()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean prepare2Attack(GameWorld world, final String fromUserCode, final String toUserCode) {
        PlayerInfo fromPlayerInfo = world.getPlayerInfoMap().get(fromUserCode);
        NpcBrain npcBrain = world.getNpcBrainMap().get(fromUserCode);
        if (fromPlayerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN || null == npcBrain) {
            logger.warn(ErrorUtil.ERROR_1039);
            return false;
        }
        if (!checkAttackCondition(fromUserCode, toUserCode)) {
            return false;
        }
        PlayerInfo toPlayerInfo = world.getPlayerInfoMap().get(toUserCode);
        npcBrain.getRedQueue().add(toPlayerInfo);
        return true;
    }

    private void hunt(GameWorld world, String npcUserCode) {
        PlayerInfo npcPlayerInfo = world.getPlayerInfoMap().get(npcUserCode);
        NpcBrain npcBrain = world.getNpcBrainMap().get(npcUserCode);
        BigDecimal distance = BlockUtil.calculateDistance(
                world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo,
                npcBrain.getRedQueue().peek());
        if (null == distance) {
            return;
        }
        BigDecimal stopDistance = npcPlayerInfo.getSkills().stream()
                .map(Skill::getRange)
                .max(BigDecimal::compareTo)
                .get()
                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        switch (npcBrain.getStance()) {
            case CreatureConstants.STANCE_DEFENSIVE:
                npcBrain.getYellowQueue().push(new WorldCoordinate(npcPlayerInfo));
            case CreatureConstants.STANCE_AGGRESSIVE:
                break;
            case CreatureConstants.STANCE_STAND_GROUND:
                stopDistance = distance;
                break;
            case CreatureConstants.STANCE_NO_ATTACK:
            default:
                return;
        }
        JSONObject moveReq = new JSONObject();
        moveReq.put("userCode", npcUserCode);
        moveReq.put("wc", npcBrain.getRedQueue().peek());
        moveReq.put("stopDistance", stopDistance);
        JSONObject moveResp = runNpcMoveTask(moveReq);
        for (int i = 0; i < npcPlayerInfo.getSkills().size(); i++) {
            if (npcPlayerInfo.getSkills().get(i).getSkillType() == SkillConstants.SKILL_TYPE_ATTACK
                    && distance.compareTo(npcPlayerInfo.getSkills().get(i).getRange()) <= 0) {
                playerService.useSkill(npcPlayerInfo.getId(), i, true);
                playerService.useSkill(npcPlayerInfo.getId(), i, false);
            }
        }
    }

    @Override
    public void resetNpcBrainQueues(String npcUserCode) {
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        Map<String, NpcBrain> npcBrainMap = world.getNpcBrainMap();
        NpcBrain npcBrain = npcBrainMap.get(npcUserCode);
        if (null != npcBrain) {
            npcBrain.getGreenQueue().clear();
            npcBrain.getYellowQueue().clear();
            npcBrain.getGreenQueue().clear();
        }
    }

    private JSONObject runNpcMoveTask(JSONObject request) {
        JSONObject rst = ContentUtil.generateRst();
        String npcUserCode = request.getString("userCode");
        WorldCoordinate wc = request.getObject("wc", WorldCoordinate.class);
        if (null == wc) {
            return rst;
        }
        double stopDistance = request.getDouble("stopDistance");
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        PlayerInfo npcPlayerInfo = world.getPlayerInfoMap().get(npcUserCode);
        BigDecimal distanceBigDecimal = BlockUtil.calculateDistance(
                world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, wc);
        if (null == distanceBigDecimal) {
            return rst;
        }
        double distance = distanceBigDecimal.doubleValue();
        if (npcPlayerInfo.getRegionNo() != wc.getRegionNo() || distance <= stopDistance) {
            npcPlayerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            // Aim at target 24/08/08
            npcPlayerInfo.setFaceDirection(BlockUtil.calculateAngle(
                    world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, wc));
            return rst;
        }
        // Speed logics, sync with front-end 24/08/24
        double newSpeed = Math.sqrt(Math.pow(npcPlayerInfo.getSpeed().getX().doubleValue(), 2)
                + Math.pow(npcPlayerInfo.getSpeed().getY().doubleValue(), 2)) + npcPlayerInfo.getAcceleration().doubleValue();
        newSpeed = Math.min(newSpeed, distance - stopDistance);
        if (npcPlayerInfo.getBuff()[GamePalConstants.BUFF_CODE_STUNNED] != 0) {
            newSpeed = 0D;
        } else if (npcPlayerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] != 0) {
            newSpeed = Math.min(npcPlayerInfo.getMaxSpeed().doubleValue() * 0.25, newSpeed);
        } else if (npcPlayerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] != 0) {
            newSpeed = Math.min(npcPlayerInfo.getMaxSpeed().doubleValue() * 0.25, newSpeed);
        } else if (npcPlayerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] != 0) {
            newSpeed = Math.min(npcPlayerInfo.getMaxSpeed().doubleValue() * 0.25, newSpeed);
        } else {
            newSpeed = Math.min(npcPlayerInfo.getMaxSpeed().doubleValue(), newSpeed);
        }
        npcPlayerInfo.setSpeed(new Coordinate(BigDecimal.valueOf(
                newSpeed * Math.cos(npcPlayerInfo.getFaceDirection().doubleValue() / 180 * Math.PI)),
                BigDecimal.valueOf(-1 * newSpeed * Math.sin(npcPlayerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));

        npcPlayerInfo.setFaceDirection(BlockUtil.calculateAngle(world.getRegionMap().get(npcPlayerInfo.getRegionNo()),
                npcPlayerInfo, wc));
        return rst;
    }
}
