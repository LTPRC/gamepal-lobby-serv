package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.factory.CreatureFactory;
import com.github.ltprc.gamepal.manager.BuffManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.Skill;
import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.item.Tool;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
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
    private MovementManager movementManager;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private BuffManager buffManager;

    @Autowired
    private WorldService worldService;

    @Override
    public Block createCreature(GameWorld world, final int playerType, final int creatureType, String userCode) {

        WorldCoordinate worldCoordinate = new WorldCoordinate();
        BlockUtil.copyWorldCoordinate(GamePalConstants.DEFAULT_BIRTHPLACE, worldCoordinate);

        BlockInfo blockInfo = BlockUtil.createBlockInfoByType(BlockConstants.BLOCK_TYPE_PLAYER);

        MovementInfo movementInfo = new MovementInfo();
        movementInfo.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        movementInfo.setFaceDirection(BlockConstants.FACE_DIRECTION_DEFAULT);
        BlockUtil.updateMaxSpeed(movementInfo);
        movementInfo.setAcceleration(BlockConstants.ACCELERATION_DEFAULT);

        Block player = new Block(worldCoordinate, blockInfo, movementInfo);
        player.getBlockInfo().setId(userCode);
        player.getMovementInfo().setFaceDirection(BigDecimal.valueOf(Math.random() * 360D));
        PlayerInfo playerInfo = CreatureFactory.createCreatureInstance(playerType);
        playerInfo.setCreatureType(creatureType);
        playerInfo.setTopBossId(userCode);
        BlockUtil.updatePerceptionInfo(playerInfo.getPerceptionInfo(), world.getWorldTime());
        buffManager.initializeBuff(playerInfo);
        world.getCreatureMap().put(userCode, player);
        world.getPlayerInfoMap().put(userCode, playerInfo);
        BagInfo bagInfo = new BagInfo();
        bagInfo.setId(userCode);
        world.getBagInfoMap().put(userCode, bagInfo);
        bagInfo = new BagInfo();
        bagInfo.setId(userCode);
        world.getPreservedBagInfoMap().put(userCode, bagInfo);
        return player;
    }

    @Override
    public Block putCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate) {
        Block player = world.getCreatureMap().get(userCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        BlockUtil.copyWorldCoordinate(worldCoordinate, player.getWorldCoordinate());
        BlockUtil.fixWorldCoordinate(world.getRegionMap().get(worldCoordinate.getRegionNo()), player.getWorldCoordinate());
        worldService.expandByCoordinate(world, null, player.getWorldCoordinate(),
                playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN ? 1 : 0);
        worldService.registerOnline(world, player.getBlockInfo());
        world.getFlagMap().putIfAbsent(userCode, new boolean[FlagConstants.FLAG_LENGTH]);
        userService.addUserIntoWorldMap(userCode, world.getId());
        if (CreatureConstants.PLAYER_TYPE_HUMAN != playerInfo.getPlayerType()) {
            NpcBrain npcBrain = generateNpcBrain();
            world.getNpcBrainMap().put(userCode, npcBrain);
            JSONObject behaviorRequest = new JSONObject();
            behaviorRequest.put("userCode", userCode);
            behaviorRequest.put("targetUserCode", userCode);
            behaviorRequest.put("behavior", CreatureConstants.NPC_BEHAVIOR_IDLE);
            JSONArray exemptionJsonArray = new JSONArray();
            if (CreatureConstants.CREATURE_TYPE_HUMAN == playerInfo.getCreatureType()) {
                behaviorRequest.put("stance", CreatureConstants.STANCE_AGGRESSIVE);
                exemptionJsonArray.add(false);
                exemptionJsonArray.add(true);
                exemptionJsonArray.add(true);
                exemptionJsonArray.add(false);
            } else {
                switch (playerInfo.getSkinColor()) {
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
                switch (playerInfo.getSkinColor()) {
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
        return player;
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
        Block player = world.getCreatureMap().get(npcUserCode);
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
                npcBrain.getGreenQueue().add(new WorldCoordinate(player.getWorldCoordinate()));
                break;
            case CreatureConstants.NPC_BEHAVIOR_FOLLOW:
                npcBrain.getGreenQueue().add(world.getCreatureMap().get(targetUserCode).getWorldCoordinate());
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
    public Block putSpecificCreature(GameWorld world, String userCode, WorldCoordinate worldCoordinate,
                                          final BigDecimal distance, final int playerType, final int creatureType,
                                          final int behavior, final int stance, final boolean[] exemption) {
        String npcUserCode = UUID.randomUUID().toString();
        Block player = createCreature(world, playerType, creatureType, npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
        worldCoordinate.getCoordinate().setX(worldCoordinate.getCoordinate().getX()
                .add(distance.multiply(BigDecimal.valueOf(
                        1 * Math.cos(player.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI)))));
        worldCoordinate.getCoordinate().setY(worldCoordinate.getCoordinate().getY()
                .subtract(distance.multiply(BigDecimal.valueOf(
                        1 * Math.sin(player.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI)))));
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
        return player;
    }

    @Override
    public Block putSpecificCreatureByRole(GameWorld world, String userCode, WorldCoordinate worldCoordinate,
                                           int role) {
        Block player;
        switch (role) {
            case CreatureConstants.NPC_ROLE_PEER:
                player = putSpecificCreature(world, userCode, worldCoordinate, BigDecimal.ONE,
                        CreatureConstants.PLAYER_TYPE_NPC, CreatureConstants.CREATURE_TYPE_HUMAN,
                        CreatureConstants.NPC_BEHAVIOR_MOVE, CreatureConstants.STANCE_DEFENSIVE,
                        new boolean[]{false, false, true, false});
                PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
                if (StringUtils.isBlank(playerInfo.getBossId())) {
                    playerService.setMember(player.getBlockInfo().getId(), player.getBlockInfo().getId(), userCode);
                } else {
                    playerService.setMember(player.getBlockInfo().getId(), player.getBlockInfo().getId(),
                            playerInfo.getBossId());
                }
                break;
            case CreatureConstants.NPC_ROLE_MINION:
                player = putSpecificCreature(world, userCode, worldCoordinate, BigDecimal.ONE,
                        CreatureConstants.PLAYER_TYPE_NPC, CreatureConstants.CREATURE_TYPE_HUMAN,
                        CreatureConstants.NPC_BEHAVIOR_FOLLOW, CreatureConstants.STANCE_DEFENSIVE,
                        new boolean[]{false, false, true, false});
                playerService.setMember(player.getBlockInfo().getId(), player.getBlockInfo().getId(), userCode);
                break;
            default:
            case CreatureConstants.NPC_ROLE_INDIVIDUAL:
                player = putSpecificCreature(world, userCode, worldCoordinate, BigDecimal.ONE,
                        CreatureConstants.PLAYER_TYPE_NPC, CreatureConstants.CREATURE_TYPE_HUMAN,
                        CreatureConstants.NPC_BEHAVIOR_IDLE, CreatureConstants.STANCE_DEFENSIVE,
                        new boolean[]{false, false, true, false});
                break;
        }
        return player;
    }

    @Override
    public void updateNpcBrains(GameWorld world) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        Map<String, NpcBrain> npcBrainMap = world.getNpcBrainMap();
        Map<String, BagInfo> bagInfoMap = world.getBagInfoMap();
        Map<String, Item> itemMap = worldService.getItemMap();
        npcBrainMap.entrySet().stream()
                // Filtered NPCs not detected by human players 24/08/01
                .filter(entry2 -> creatureMap.entrySet().stream()
                        .filter(entry3 -> CreatureConstants.PLAYER_TYPE_HUMAN == world.getPlayerInfoMap().get(entry3.getKey()).getPlayerType())
                        .anyMatch(entry3 -> SkillUtil.isSceneDetected(entry3.getValue(),
                                creatureMap.get(entry3.getKey()).getWorldCoordinate(), 1)))
                .filter(entry2 -> playerService.validateActiveness(world, entry2.getKey()))
                .forEach(entry2 -> {
                    String npcUserCode = entry2.getKey();
                    Block player = creatureMap.get(npcUserCode);
                    PlayerInfo playerInfo = playerInfoMap.get(npcUserCode);
                    NpcBrain npcBrain = entry2.getValue();
                    // Abandon unloaded weapon 25/01/21
                    BagInfo bagInfo = bagInfoMap.get(npcUserCode);
                    playerInfo.getTools().stream()
                            .filter(itemMap::containsKey)
                            .forEach(toolStr -> {
                                Tool tool = (Tool) itemMap.get(toolStr);
                                if (tool.getSkills().stream().noneMatch(skill ->
                                        bagInfo.getItems().getOrDefault(skill.getAmmoCode(), 0) > 0)) {
                                    playerService.useItem(npcUserCode, toolStr, 1);
                                }
                            });
                    // Old targets
                    WorldCoordinate oldWc = npcBrain.getGreenQueue().peek();
                    if (null != oldWc) {
                        BigDecimal distance = BlockUtil.calculateDistance(
                                world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                player.getWorldCoordinate(), oldWc);
                        if (npcBrain.getBehavior() == CreatureConstants.NPC_BEHAVIOR_PATROL && null != distance
                                && distance.compareTo(CreatureConstants.NPC_ARRIVE_DISTANCE) <= 0) {
                            npcBrain.getGreenQueue().add(npcBrain.getGreenQueue().poll());
                        }
                    }
                    oldWc = npcBrain.getYellowQueue().peek();
                    if (null != oldWc) {
                        BigDecimal distance = BlockUtil.calculateDistance(
                                world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                player.getWorldCoordinate(), oldWc);
                        if (null == distance || distance.compareTo(CreatureConstants.NPC_ARRIVE_DISTANCE) <= 0) {
                            npcBrain.getYellowQueue().pop();
                        }
                    }
                    Optional<Block> red = creatureMap.values().stream()
                            .filter(player1 -> !npcUserCode.equals(player1.getBlockInfo().getId()))
                            .filter(player1 -> playerService.validateActiveness(world, player1.getBlockInfo().getId()))
                            .filter(player1 -> BlockUtil.checkPerceptionCondition(
                                    world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()), player,
                                    world.getPlayerInfoMap().get(npcUserCode).getPerceptionInfo(), player1))
                            .min((player1, player2) -> {
                                BigDecimal distance1 = BlockUtil.calculateDistance
                                        (world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                                player.getWorldCoordinate(), player1.getWorldCoordinate());
                                BigDecimal distance2 = BlockUtil.calculateDistance
                                        (world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                                player.getWorldCoordinate(), player2.getWorldCoordinate());
                                assert distance1 != null;
                                return distance1.compareTo(distance2);
                            });
                    // Innocent exemption check must be here, otherwise self-defense would not work 24/08/08
                    if (red.isPresent() && !npcBrain.getExemption()[CreatureConstants.NPC_EXEMPTION_INNOCENT]) {
                        prepare2Attack(world, npcUserCode, red.get().getBlockInfo().getId());
                    }
                    // Remove not alive element from red queue 24/08/08
                    while (!npcBrain.getRedQueue().isEmpty() && !playerService.validateActiveness(world,
                            npcBrain.getRedQueue().peek().getBlockInfo().getId())) {
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
        creatureMap.entrySet().stream()
                .filter(entry2 -> world.getPlayerInfoMap().get(entry2.getKey()).getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN)
                .filter(entry2 -> playerService.validateActiveness(world, entry2.getKey()))
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
        Block fromPlayer = world.getCreatureMap().get(fromUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(fromUserCode);
        NpcBrain npcBrain = world.getNpcBrainMap().get(fromUserCode);
        if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN || null == npcBrain) {
            logger.warn(ErrorUtil.ERROR_1039);
            return false;
        }
        if (!checkAttackCondition(fromUserCode, toUserCode)) {
            return false;
        }
        Block toPlayer = world.getCreatureMap().get(toUserCode);
        npcBrain.getRedQueue().add(toPlayer);
        return true;
    }

    private void hunt(GameWorld world, String npcUserCode) {
        Block npcPlayer = world.getCreatureMap().get(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        NpcBrain npcBrain = world.getNpcBrainMap().get(npcUserCode);
        BigDecimal distance = BlockUtil.calculateDistance(
                world.getRegionMap().get(npcPlayer.getWorldCoordinate().getRegionNo()), npcPlayer.getWorldCoordinate(),
                npcBrain.getRedQueue().peek().getWorldCoordinate());
        if (null == distance) {
            return;
        }
        BigDecimal stopDistance = playerInfo.getSkills().stream()
                .map(Skill::getRange)
                .max(BigDecimal::compareTo)
                .get()
                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        switch (npcBrain.getStance()) {
            case CreatureConstants.STANCE_DEFENSIVE:
                npcBrain.getYellowQueue().push(new WorldCoordinate(npcPlayer.getWorldCoordinate()));
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
        moveReq.put("redBlock", npcBrain.getRedQueue().peek());
        moveReq.put("stopDistance", stopDistance);
        JSONObject moveResp = runNpcMoveTask(moveReq);
        for (int i = 0; i < playerInfo.getSkills().size(); i++) {
            if (playerInfo.getSkills().get(i).getSkillType() == SkillConstants.SKILL_TYPE_ATTACK
                    && distance.compareTo(playerInfo.getSkills().get(i).getRange()) <= 0) {
                playerService.useSkill(npcPlayer.getBlockInfo().getId(), i, true);
                playerService.useSkill(npcPlayer.getBlockInfo().getId(), i, false);
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
        Block redBlock = request.getObject("redBlock", Block.class);
        if (null == redBlock) {
            return rst;
        }
        WorldCoordinate wc = redBlock.getWorldCoordinate();
        double stopDistance = request.getDouble("stopDistance");
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        Block npcPlayer = world.getCreatureMap().get(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
        BigDecimal distanceBigDecimal = BlockUtil.calculateDistance(
                world.getRegionMap().get(npcPlayer.getWorldCoordinate().getRegionNo()), npcPlayer.getWorldCoordinate(), wc);
        if (null == distanceBigDecimal) {
            return rst;
        }
        double distance = distanceBigDecimal.doubleValue();
        if (npcPlayer.getWorldCoordinate().getRegionNo() != wc.getRegionNo() || distance <= stopDistance) {
            npcPlayer.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            // Aim at target 24/08/08
            npcPlayer.getMovementInfo().setFaceDirection(BlockUtil.calculateAngle(
                    world.getRegionMap().get(npcPlayer.getWorldCoordinate().getRegionNo()), npcPlayer.getWorldCoordinate(), wc));
            return rst;
        }
        // Speed logics, sync with front-end 24/08/24
        double newSpeed = Math.sqrt(Math.pow(npcPlayer.getMovementInfo().getSpeed().getX().doubleValue(), 2)
                + Math.pow(npcPlayer.getMovementInfo().getSpeed().getY().doubleValue(), 2)) + npcPlayer.getMovementInfo().getAcceleration().doubleValue();
        newSpeed = Math.min(newSpeed, distance - stopDistance);
        if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_STUNNED] != 0) {
            newSpeed = 0D;
        } else if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FRACTURED] != 0) {
            newSpeed = Math.min(npcPlayer.getMovementInfo().getMaxSpeed().doubleValue() * 0.25, newSpeed);
        } else if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_OVERWEIGHTED] != 0) {
            newSpeed = Math.min(npcPlayer.getMovementInfo().getMaxSpeed().doubleValue() * 0.25, newSpeed);
        } else if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_FATIGUED] != 0) {
            newSpeed = Math.min(npcPlayer.getMovementInfo().getMaxSpeed().doubleValue() * 0.25, newSpeed);
        } else {
            newSpeed = Math.min(npcPlayer.getMovementInfo().getMaxSpeed().doubleValue(), newSpeed);
        }
        npcPlayer.getMovementInfo().setSpeed(new Coordinate(BigDecimal.valueOf(
                newSpeed * Math.cos(npcPlayer.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI)),
                BigDecimal.valueOf(-1 * newSpeed * Math.sin(npcPlayer.getMovementInfo().getFaceDirection().doubleValue() / 180 * Math.PI))));

        npcPlayer.getMovementInfo().setFaceDirection(BlockUtil.calculateAngle(world.getRegionMap().get(npcPlayer.getWorldCoordinate().getRegionNo()),
                npcPlayer.getWorldCoordinate(), wc));
        return rst;
    }
}
