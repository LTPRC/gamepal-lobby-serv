package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.PlayerInfoFactory;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.npc.NpcBrain;
import com.github.ltprc.gamepal.model.npc.NpcMoveTask;
import com.github.ltprc.gamepal.model.npc.NpcObserveTask;
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
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

@Component
public class NpcManagerImpl implements NpcManager {

    private static final Log logger = LogFactory.getLog(NpcManagerImpl.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerInfoFactory playerInfoFactory;

    @Override
    public String createNpc(GameWorld world) {
        String userCode = UUID.randomUUID().toString();
        PlayerInfo npcPlayerInfo = playerInfoFactory.createPlayerInfoInstance();
        npcPlayerInfo.setId(userCode);
        npcPlayerInfo.setPlayerType(GamePalConstants.PLAYER_TYPE_AI);
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
        npcBrain.setObserveTaskQueue(new PriorityQueue<>());
        npcBrain.setMoveTaskQueue(new PriorityQueue<>());
        npcBrain.setAttackTaskQueue(new PriorityQueue<>());
        npcBrain.getObserveTaskQueue().add(new NpcObserveTask());
        npcBrain.getMoveTaskQueue().add(new NpcMoveTask());
        return npcBrain;
    }

    @Override
    public JSONObject runNpcTask(JSONObject request) {
        JSONObject rst = ContentUtil.generateRst();
        switch (request.getInteger("npcTaskType")) {
            case GamePalConstants.NPC_TASK_TYPE_IDLE:
                rst = runNpcIdleTask(request);
                break;
            case GamePalConstants.NPC_TASK_TYPE_OBSERVE:
                rst = runNpcObserveTask(request);
                break;
            case GamePalConstants.NPC_TASK_TYPE_MOVE:
                rst = runNpcMoveTask(request);
                break;
            case GamePalConstants.NPC_TASK_TYPE_ATTACK:
                break;
            default:
                break;
        }
        return rst;
    }

    private JSONObject runNpcIdleTask(JSONObject request) {
        JSONObject rst = ContentUtil.generateRst();
        String npcUserCode = request.getString("userCode");
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        return rst;
    }

    private JSONObject runNpcObserveTask(JSONObject request) {
        JSONObject rst = ContentUtil.generateRst();
        String npcUserCode = request.getString("userCode");
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        rst.put("minDistance", GamePalConstants.NPC_MAX_OBSERVE_RANGE);
        world.getPlayerInfoMap().entrySet().stream()
                .filter(entry -> !entry.getValue().getId().equals(npcUserCode))
                .filter(entry -> entry.getValue().getRegionNo() == playerInfo.getRegionNo())
                .filter(entry -> SkillUtil.validateDamage(entry.getValue()))
                .forEach(entry -> {
                    BigDecimal distance = BlockUtil.calculateDistance(world.getRegionMap().get(playerInfo.getRegionNo()),
                            playerInfo, entry.getValue());
                    if (distance.compareTo(rst.getBigDecimal("minDistance")) < 0
                            && distance.compareTo(GamePalConstants.NPC_MAX_CHASE_DISTANCE) > 0) {
                        rst.put("minDistance", distance);
                        WorldCoordinate wc = new WorldCoordinate();
                        wc.setRegionNo(entry.getValue().getRegionNo());
                        wc.setSceneCoordinate(entry.getValue().getSceneCoordinate());
                        wc.setCoordinate(entry.getValue().getCoordinate());
                        rst.put("wc", wc);
                    }
                });
        return rst;
    }

    private JSONObject runNpcMoveTask(JSONObject request) {
        JSONObject rst = ContentUtil.generateRst();
        String npcUserCode = request.getString("userCode");
        WorldCoordinate wc = request.getObject("wc", WorldCoordinate.class);
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        BigDecimal distanceBigDecimal = BlockUtil.calculateDistance(
                world.getRegionMap().get(playerInfo.getRegionNo()), playerInfo, wc);
        if (null == distanceBigDecimal) {
            logger.warn(ErrorUtil.ERROR_1034);
            return rst;
        }
        double distance = distanceBigDecimal.doubleValue();
        double stopDistance = GamePalConstants.PLAYER_RADIUS.doubleValue() * 2;
        if (playerInfo.getRegionNo() != wc.getRegionNo() || distance <= stopDistance) {
            playerInfo.setFaceDirection(BigDecimal.ZERO);
            playerInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            return rst;
        }
        double newSpeed = Math.sqrt(Math.pow(playerInfo.getSpeed().getX().doubleValue(), 2)
                + Math.pow(playerInfo.getSpeed().getY().doubleValue(), 2)) + playerInfo.getAcceleration().doubleValue();
        double maxSpeed = playerInfo.getVp() > 0 ? playerInfo.getMaxSpeed().doubleValue()
                : playerInfo.getMaxSpeed().doubleValue() * 0.5D;
        newSpeed = Math.min(newSpeed, maxSpeed);
        newSpeed = Math.min(newSpeed, distance - stopDistance);
        playerInfo.setFaceDirection(BlockUtil.calculateAngle(world.getRegionMap().get(playerInfo.getRegionNo()),
                playerInfo, wc));
        playerInfo.setSpeed(new Coordinate(BigDecimal.valueOf(
                newSpeed * Math.cos(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI)),
                BigDecimal.valueOf(-1 * newSpeed * Math.sin(playerInfo.getFaceDirection().doubleValue() / 180 * Math.PI))));
        return rst;
    }
}
