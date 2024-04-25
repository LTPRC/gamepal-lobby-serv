package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.PlayerConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.factory.PlayerInfoFactory;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.npc.NpcBrain;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class NpcManagerImpl implements NpcManager {

    private static final Log logger = LogFactory.getLog(NpcManagerImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerInfoFactory playerInfoFactory;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private PlayerService playerService;

    @Override
    public String createNpc(GameWorld world) {
        String userCode = UUID.randomUUID().toString();
        PlayerInfo npcPlayerInfo = playerInfoFactory.createPlayerInfoInstance();
        npcPlayerInfo.setId(userCode);
        npcPlayerInfo.setPlayerType(PlayerConstants.PLAYER_TYPE_AI);
        npcPlayerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_INIT);
        world.getPlayerInfoMap().put(userCode, npcPlayerInfo);
        userService.addUserIntoWorldMap(world, userCode);
        NpcBrain npcBrain = generateNpcBrain();
        world.getNpcBrainMap().put(userCode, npcBrain);
        return userCode;
    }

    @Override
    public void putNpc(String userCode, String npcUserCode) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        PlayerInfo npcPlayerInfo = playerInfoMap.get(npcUserCode);
        npcPlayerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
        BlockUtil.copyWorldCoordinate(playerInfoMap.get(userCode), playerInfoMap.get(npcUserCode));
        npcPlayerInfo.setFaceDirection(BigDecimal.valueOf(Math.random() * 360D));
        npcPlayerInfo.setRegionNo(playerInfo.getRegionNo());
        npcPlayerInfo.setSceneCoordinate(playerInfo.getSceneCoordinate());
        npcPlayerInfo.getCoordinate().setX(playerInfo.getCoordinate().getX()
                .add(BigDecimal.valueOf(1 * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
        npcPlayerInfo.getCoordinate().setY(playerInfo.getCoordinate().getY()
                .subtract(BigDecimal.valueOf(1 * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
        BlockUtil.fixWorldCoordinate(world.getRegionMap().get(playerInfo.getRegionNo()), npcPlayerInfo);
        world.getOnlineMap().put(npcUserCode, -1L);
    }

    private NpcBrain generateNpcBrain() {
        NpcBrain npcBrain = new NpcBrain();
        npcBrain.setPeaceWithTeammate(true);
        npcBrain.setPeaceWithSameCreature(false);
        npcBrain.setBehavior(PlayerConstants.NPC_BEHAVIOR_IDLE);
        npcBrain.setStance(PlayerConstants.STANCE_AGGRESSIVE);
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
        PlayerInfo npcPlayerInfo = world.getPlayerInfoMap().get(npcUserCode);
        Map<String, NpcBrain> npcBrainMap = world.getNpcBrainMap();
        NpcBrain npcBrain = npcBrainMap.get(npcUserCode);
        resetNpcBrainQueues(npcUserCode);
        String targetUserCode = request.getString("targetUserCode");
        WorldCoordinate targetWorldCoordinate = request.getObject("targetWorldCoordinate", WorldCoordinate.class);
        int behavior = request.getInteger("npcBehaviorType");
        npcBrain.setBehavior(behavior);
        switch (behavior) {
            case PlayerConstants.NPC_BEHAVIOR_MOVE:
            case PlayerConstants.NPC_BEHAVIOR_GUARD:
                npcBrain.getGreenQueue().add(targetWorldCoordinate);
                break;
            case PlayerConstants.NPC_BEHAVIOR_PATROL:
                npcBrain.getGreenQueue().add(targetWorldCoordinate);
                npcBrain.getGreenQueue().add(new WorldCoordinate(npcPlayerInfo));
                break;
            case PlayerConstants.NPC_BEHAVIOR_FOLLOW:
                npcBrain.getGreenQueue().add(world.getPlayerInfoMap().get(targetUserCode));
                break;
            case PlayerConstants.NPC_BEHAVIOR_IDLE:
            default:
                break;
        }
        return rst;
    }

    @Override
    public void updateNpcBrains(GameWorld world) {
        Map<String, NpcBrain> npcBrainMap = world.getNpcBrainMap();
        npcBrainMap.entrySet().stream()
                .filter(entry2 -> SkillUtil.validateDamage(world.getPlayerInfoMap().get(entry2.getKey())))
                .forEach(entry2 -> {
                    String npcUserCode = entry2.getKey();
                    PlayerInfo npcPlayerInfo = world.getPlayerInfoMap().get(npcUserCode);
                    NpcBrain npcBrain = entry2.getValue();
                    // Old targets
                    if (!npcBrain.getGreenQueue().isEmpty()) {
                        WorldCoordinate oldWc = npcBrain.getGreenQueue().peek();
                        BigDecimal distance = BlockUtil.calculateDistance(
                                world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, oldWc);
                        if (npcBrain.getBehavior() == PlayerConstants.NPC_BEHAVIOR_PATROL && null != distance
                                && distance.compareTo(PlayerConstants.NPC_ARRIVE_DISTANCE) <= 0) {
                            npcBrain.getGreenQueue().add(npcBrain.getGreenQueue().poll());
                        }
                    }
                    if (!npcBrain.getYellowQueue().isEmpty()) {
                        WorldCoordinate oldWc = npcBrain.getYellowQueue().peek();
                        BigDecimal distance = BlockUtil.calculateDistance(
                                world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo, oldWc);
                        if (null == distance || distance.compareTo(PlayerConstants.NPC_ARRIVE_DISTANCE) <= 0) {
                            npcBrain.getYellowQueue().pop();
                        }
                    }
                    Optional<PlayerInfo> red = world.getPlayerInfoMap().values().stream()
                            .filter(playerInfo -> !npcUserCode.equals(playerInfo.getId()))
                            .filter(SkillUtil::validateDamage)
                            .filter(playerInfo -> checkAttackCondition(npcUserCode, playerInfo.getId()))
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
                    if (red.isPresent()) {
                        npcBrain.getRedQueue().clear();
                        npcBrain.getRedQueue().add(red.get());
                    } else if (!npcBrain.getRedQueue().isEmpty()) {
                        PlayerInfo oldPlayerInfo = npcBrain.getRedQueue().poll();
                        if (npcBrain.getRedQueue().isEmpty()) {
                            npcBrain.getYellowQueue().push(new WorldCoordinate(oldPlayerInfo));
                        }
                    }
                    // Move & Destroy
                    if (!npcBrain.getRedQueue().isEmpty()
                            && npcPlayerInfo.getSkill()[0].getSkillType() == SkillConstants.SKILL_TYPE_ATTACK) {
                        switch (npcBrain.getStance()) {
                            case PlayerConstants.STANCE_DEFENSIVE:
                                npcBrain.getYellowQueue().push(new WorldCoordinate(npcPlayerInfo));
                            case PlayerConstants.STANCE_AGGRESSIVE:
                                JSONObject moveReq = new JSONObject();
                                moveReq.put("userCode", npcUserCode);
                                moveReq.put("wc", npcBrain.getRedQueue().peek());
                                moveReq.put("stopDistance", npcPlayerInfo.getSkill()[0].getRange()
                                        .divide(BigDecimal.valueOf(2)));
                                JSONObject moveResp = runNpcMoveTask(moveReq);
                                BigDecimal distance = BlockUtil.calculateDistance(
                                        world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo,
                                        npcBrain.getRedQueue().peek());
                                for (int i = 0; i < npcPlayerInfo.getSkill().length; i++) {
                                    if (distance.compareTo(npcPlayerInfo.getSkill()[i].getRange()) <= 0) {
                                        playerService.useSkill(npcPlayerInfo.getId(), i, true);
                                        playerService.useSkill(npcPlayerInfo.getId(), i, false);
                                    }
                                }
                                break;
                            case PlayerConstants.STANCE_STAND_GROUND:
                                moveReq = new JSONObject();
                                moveReq.put("userCode", npcUserCode);
                                moveReq.put("wc", npcBrain.getRedQueue().peek());
                                moveReq.put("stopDistance", npcPlayerInfo.getSkill()[0].getRange()
                                        .divide(BigDecimal.valueOf(2)));
                                moveResp = runNpcMoveTask(moveReq);
                                npcPlayerInfo.setSpeed(new Coordinate());
                                distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(npcPlayerInfo.getRegionNo()), npcPlayerInfo,
                                    npcBrain.getRedQueue().peek());
                                for (int i = 0; i < npcPlayerInfo.getSkill().length; i++) {
                                    if (distance.compareTo(npcPlayerInfo.getSkill()[i].getRange()) <= 0) {
                                        playerService.useSkill(npcPlayerInfo.getId(), i, true);
                                        playerService.useSkill(npcPlayerInfo.getId(), i, false);
                                    }
                                }
                                break;
                            case PlayerConstants.STANCE_NO_ATTACK:
                            default:
                                break;
                        }
                    } else if (!npcBrain.getYellowQueue().isEmpty()) {
                        JSONObject moveReq = new JSONObject();
                        moveReq.put("userCode", npcUserCode);
                        moveReq.put("wc", npcBrain.getYellowQueue().peek());
                        moveReq.put("stopDistance", PlayerConstants.NPC_ARRIVE_DISTANCE);
                        JSONObject moveResp = runNpcMoveTask(moveReq);
                    } else if (!npcBrain.getGreenQueue().isEmpty()) {
                        JSONObject moveReq = new JSONObject();
                        moveReq.put("userCode", npcUserCode);
                        moveReq.put("wc", npcBrain.getGreenQueue().peek());
                        moveReq.put("stopDistance", npcBrain.getBehavior() == PlayerConstants.NPC_BEHAVIOR_FOLLOW
                                ? PlayerConstants.NPC_FOLLOW_STOP_DISTANCE : PlayerConstants.NPC_ARRIVE_DISTANCE);
                        JSONObject moveResp = runNpcMoveTask(moveReq);
                    }
                });
        // Settle NPC speed
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        playerInfoMap.entrySet().stream()
                .filter(entry2 -> entry2.getValue().getPlayerType() != PlayerConstants.PLAYER_TYPE_HUMAN)
                .filter(entry2 -> SkillUtil.validateDamage(entry2.getValue()))
                .forEach(entry2 -> movementManager.settleSpeed(entry2.getKey(), entry2.getValue()));
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
        if (world.getNpcBrainMap().get(fromUserCode).getStance() == PlayerConstants.STANCE_NO_ATTACK
                || world.getPlayerInfoMap().get(fromUserCode).getSkill()[0].getSkillType()
                != SkillConstants.SKILL_TYPE_ATTACK) {
            return false;
        }
        if (world.getNpcBrainMap().get(fromUserCode).isPeaceWithSameCreature()
                && world.getPlayerInfoMap().get(toUserCode).getCreature()
                .equals(world.getPlayerInfoMap().get(toUserCode).getCreature())) {
            return false;
        } else if (world.getNpcBrainMap().get(fromUserCode).isPeaceWithTeammate()
                && playerService.findTopBossId(fromUserCode).equals(playerService.findTopBossId(toUserCode))) {
            return false;
        }
        return true;
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
            return rst;
        }
        double newSpeed = Math.sqrt(Math.pow(npcPlayerInfo.getSpeed().getX().doubleValue(), 2)
                + Math.pow(npcPlayerInfo.getSpeed().getY().doubleValue(), 2)) + npcPlayerInfo.getAcceleration().doubleValue();
        double maxSpeed = npcPlayerInfo.getVp() > 0 ? npcPlayerInfo.getMaxSpeed().doubleValue()
                : npcPlayerInfo.getMaxSpeed().doubleValue() * 0.5D;
        newSpeed = Math.min(newSpeed, maxSpeed);
        newSpeed = Math.min(newSpeed, distance - stopDistance);
        npcPlayerInfo.setFaceDirection(BlockUtil.calculateAngle(world.getRegionMap().get(npcPlayerInfo.getRegionNo()),
                npcPlayerInfo, wc));
        npcPlayerInfo.setSpeed(new Coordinate(BigDecimal.valueOf(
                newSpeed * Math.cos(npcPlayerInfo.getFaceDirection().doubleValue() / 180 * Math.PI)),
                BigDecimal.valueOf(-1 * newSpeed * Math.sin(npcPlayerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
        return rst;
    }
}
