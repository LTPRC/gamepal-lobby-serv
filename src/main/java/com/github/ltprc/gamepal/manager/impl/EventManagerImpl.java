package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class EventManagerImpl implements EventManager {

    private static final Log logger = LogFactory.getLog(EventManagerImpl.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private NpcManager npcManager;

    @Override
    public MovementInfo createMovementInfoByEventCode(final int eventCode) {
        MovementInfo movementInfo = new MovementInfo();
        movementInfo.setFrame(0);
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_MINE:
                // Infinite
                movementInfo.setFrameMax(-1);
                break;
            case GamePalConstants.EVENT_CODE_SPARK_SHORT:
                movementInfo.setFrameMax(1);
                break;
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                movementInfo.setFrameMax(50);
                break;
            case GamePalConstants.EVENT_CODE_FIRE:
                movementInfo.setFrameMax(250);
                break;
            default:
                movementInfo.setFrameMax(25);
                break;
        }
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                movementInfo.setPeriod(50);
                break;
            default:
                movementInfo.setPeriod(25);
                break;
        }
        return movementInfo;
    }

    @Override
    public void addEvent(GameWorld world, int eventCode, String eventId, WorldCoordinate worldCoordinate) {
        MovementInfo movementInfo = createMovementInfoByEventCode(eventCode);
        Block eventBlock = sceneManager.addEventBlock(world, eventCode, eventId, movementInfo, worldCoordinate);
        correctTarget(world, eventBlock);
        List<Block> affectedBlockList = collectAffectedBlocks(world, eventBlock);
        activateEvent(world, eventBlock, affectedBlockList);
    }

    private void correctTarget(GameWorld world, Block eventBlock) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        String fromId = world.getEffectMap().get(eventBlock.getBlockInfo().getId());
        Block fromCreature = world.getCreatureMap().get(fromId);
        WorldCoordinate from = fromCreature.getWorldCoordinate();
        Region region = regionMap.get(from.getRegionNo());
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        Set<IntegerCoordinate> preSelectedSceneCoordinates =
                BlockUtil.preSelectSceneCoordinates(region, from, worldCoordinate);
        // Detect the maximum moving distance
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
                region.getScenes().get(sceneCoordinate).getBlocks().values().stream()
                        .filter(blocker -> BlockUtil.checkMaterialCollision(
                                eventBlock.getBlockInfo().getStructure().getMaterial(),
                                blocker.getBlockInfo().getStructure().getMaterial()))
                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
                                BlockUtil.calculateAngle(region, from, eventBlock.getWorldCoordinate()).doubleValue()) < 135D)
                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
                                fromCreature.getMovementInfo().getFaceDirection().doubleValue()) < 135D
                        )
                        .forEach(blocker ->
                            BlockUtil.detectLineCollision(region, from, eventBlock, blocker, true)
                        ));
    }

    private List<Block> collectAffectedBlocks(GameWorld world, Block eventBlock) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        String fromId = world.getEffectMap().get(eventBlock.getBlockInfo().getId());
        Block fromCreature = world.getCreatureMap().get(fromId);
        WorldCoordinate from = fromCreature.getWorldCoordinate();
        Region region = regionMap.get(from.getRegionNo());
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        Set<IntegerCoordinate> preSelectedSceneCoordinates =
                BlockUtil.preSelectSceneCoordinates(region, from, worldCoordinate);
        // Pre-select blocks including creatures
        List<Block> preSelectedBlocks = world.getCreatureMap().values().stream()
                .filter(creature -> creature.getWorldCoordinate().getRegionNo() == region.getRegionNo())
                .filter(creature -> preSelectedSceneCoordinates.contains(creature.getWorldCoordinate().getSceneCoordinate()))
                .filter(creature -> playerService.validateActiveness(world, creature.getBlockInfo().getId()))
//                .filter(creature -> BlockUtil.checkMaterialCollision(
//                        eventBlock.getBlockInfo().getStructure().getMaterial(),
//                        creature.getBlockInfo().getStructure().getMaterial()))
                .filter(creature -> checkEventCondition(world, from, eventBlock, creature))
                .collect(Collectors.toList());
        // Collect all collided blocks
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
                region.getScenes().get(sceneCoordinate).getBlocks().values().stream()
//                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
//                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
//                                BlockUtil.calculateAngle(region, from, eventBlock.getWorldCoordinate()).doubleValue()) < 135D)
                        .filter(blocker -> checkEventCondition(world, from, eventBlock, blocker))
                        .forEach(preSelectedBlocks::add));
        return shortenPreSelectedBlocks(region, from, eventBlock, preSelectedBlocks);
    }

    private boolean checkEventCondition(GameWorld world, WorldCoordinate from, Block eventBlock, Block blocker) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(from.getRegionNo());
        BigDecimal distance = BlockUtil.calculateDistance(region, from, blocker.getWorldCoordinate());
        if (null == distance) {
            return false;
        }
        boolean rst = false;
        double angle1 = BlockUtil.calculateAngle(region, from, eventBlock.getWorldCoordinate()).doubleValue();
        double angle2 = BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue();
        double deltaAngle = BlockUtil.compareAnglesInDegrees(angle1, angle2);
        switch (Integer.parseInt(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
                rst = !world.getEffectMap().get(eventBlock.getBlockInfo().getId()).equals(blocker.getBlockInfo().getId())
                        && distance.compareTo(SkillConstants.SKILL_RANGE_MELEE) <= 0
                        && deltaAngle < SkillConstants.SKILL_ANGLE_MELEE_MAX.doubleValue();
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                rst = !world.getEffectMap().get(eventBlock.getBlockInfo().getId()).equals(blocker.getBlockInfo().getId())
                        && distance.compareTo(SkillConstants.SKILL_RANGE_SHOOT) <= 0
                        && deltaAngle < SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                        && BlockUtil.detectLineCollision(region, from, eventBlock, blocker, false);
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                BigDecimal eventDistance = BlockUtil.calculateDistance(region, eventBlock.getWorldCoordinate(),
                        blocker.getWorldCoordinate());
                rst = eventDistance.compareTo(SkillConstants.SKILL_RANGE_EXPLODE) <= 0;
                break;
            default:
                break;
        }
        return rst;
    }

    private List<Block> shortenPreSelectedBlocks(RegionInfo regionInfo, WorldCoordinate from, Block eventBlock,
                                                 List<Block> preSelectedBlocks) {
        Optional<Block> collidedBlock = preSelectedBlocks.stream()
                .filter(preSelectedBlock -> BlockUtil.checkMaterialCollision(
                        preSelectedBlock.getBlockInfo().getStructure().getMaterial(),
                        eventBlock.getBlockInfo().getStructure().getMaterial()))
                .filter(block -> null != BlockUtil.calculateDistance(regionInfo, from, block.getWorldCoordinate()))
                .min(Comparator.comparing(block -> BlockUtil.calculateDistance(regionInfo, from, block.getWorldCoordinate())));
        List<Block> shortenedBlocks = preSelectedBlocks.stream()
                .filter(preSelectedBlock -> !BlockUtil.checkMaterialCollision(
                        preSelectedBlock.getBlockInfo().getStructure().getMaterial(),
                        eventBlock.getBlockInfo().getStructure().getMaterial()))
                .collect(Collectors.toList());
        collidedBlock.ifPresent(shortenedBlocks::add);
        return shortenedBlocks;
    }

    private void activateEvent(GameWorld world, Block eventBlock, List<Block> affectedBlockList) {
        Random random = new Random();
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo());
        Map<String, Block> creatureMap = world.getCreatureMap();
        String fromId = world.getEffectMap().get(eventBlock.getBlockInfo().getId());
        Block fromPlayer = world.getCreatureMap().get(fromId);
        // Effect after activation
        switch (Integer.parseInt(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_HEAL:
                affectBlock(world, eventBlock, creatureMap.get(world.getEffectMap().get(eventBlock.getBlockInfo().getId())));
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_CHOP:
                affectedBlockList.stream()
//                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            affectBlock(world, eventBlock, player);
//                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
//                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
//                                    BigDecimal.valueOf(random.nextDouble() / 2));
//                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), bleedWc);
                });
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
                affectedBlockList.stream()
//                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            affectBlock(world, eventBlock, player);
//                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
//                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
//                                    BigDecimal.valueOf(random.nextDouble() / 2));
//                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), bleedWc);
                });
                updateBullet(world, eventBlock, affectedBlockList);
                WorldCoordinate sparkWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        world.getRegionMap().get(fromPlayer.getWorldCoordinate().getRegionNo()),
                        fromPlayer.getWorldCoordinate(),
                        fromPlayer.getMovementInfo().getFaceDirection().add(BigDecimal.valueOf(
                                SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue() * 2
                                        * (random.nextDouble() - 0.5D))), BigDecimal.ONE);
                addEvent(world, GamePalConstants.EVENT_CODE_SPARK_SHORT, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), sparkWc);
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                BigDecimal tailSmokeLength = BlockUtil.calculateDistance(regionMap.get(
                        eventBlock.getWorldCoordinate().getRegionNo()),
                        creatureMap.get(world.getEffectMap().get(eventBlock.getBlockInfo().getId())).getWorldCoordinate(),
                        eventBlock.getWorldCoordinate());
                if (null != tailSmokeLength) {
                    int tailSmokeAmount = tailSmokeLength.intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                            creatureMap.get(world.getEffectMap().get(eventBlock.getBlockInfo().getId())).getWorldCoordinate(),
                            eventBlock.getWorldCoordinate(), tailSmokeAmount);
                    equidistantPoints.stream()
                            .forEach(tailSmokeCoordinate -> {
//                                BlockUtil.fixWorldCoordinate(regionMap.get(
//                                        eventBlock.getWorldCoordinate().getRegionNo()), tailSmokeCoordinate);
                                addEvent(world, GamePalConstants.EVENT_CODE_TAIL_SMOKE, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), tailSmokeCoordinate);
                            });
                }
                updateBullet(world, eventBlock, affectedBlockList);
                addEvent(world, GamePalConstants.EVENT_CODE_EXPLODE, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), eventBlock.getWorldCoordinate());
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
                BigDecimal flameLength = BlockUtil.calculateDistance(regionMap.get(
                                eventBlock.getWorldCoordinate().getRegionNo()),
                        creatureMap.get(world.getEffectMap().get(eventBlock.getBlockInfo().getId())).getWorldCoordinate(),
                        eventBlock.getWorldCoordinate());
                if (null != flameLength) {
                    int flameAmount = flameLength.subtract(SkillConstants.SKILL_RANGE_SHOOT_FIRE_MIN).max(BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(2)).intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                            creatureMap.get(world.getEffectMap().get(eventBlock.getBlockInfo().getId())).getWorldCoordinate(),
                            eventBlock.getWorldCoordinate(), flameAmount);
                    equidistantPoints.stream()
                            .forEach(flameCoordinate -> {
//                                BlockUtil.fixWorldCoordinate(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
//                                        flameCoordinate);
                                addEvent(world, GamePalConstants.EVENT_CODE_FIRE, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), flameCoordinate);
                            });
                }
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
                addEvent(world, GamePalConstants.EVENT_CODE_WATER, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), eventBlock.getWorldCoordinate());
                // Extinguish fire
                region.getScenes().values().stream()
                        .filter(scene -> SkillUtil.isSceneDetected(eventBlock, scene.getSceneCoordinate(), 1))
                        .forEach(scene -> scene.getBlocks().values().stream()
                                .filter(block -> block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_EFFECT)
                                .filter(block -> Integer.parseInt(block.getBlockInfo().getCode()) == GamePalConstants.EVENT_CODE_WATER)
                                .filter(block -> {
                                    BigDecimal distance = BlockUtil.calculateDistance(
                                            world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                            eventBlock.getWorldCoordinate(), block.getWorldCoordinate());
                                    return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                                })
                                .forEach(block -> sceneManager.removeBlock(world, block)));
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                sceneManager.setGridBlockCode(world, eventBlock.getWorldCoordinate(), BlockConstants.BLOCK_CODE_LAVA);
                affectedBlockList.stream()
//                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            affectBlock(world, eventBlock, player);
//                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
//                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
//                                    BigDecimal.valueOf(random.nextDouble() / 2));
//                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, player.getBlockInfo().getId(), bleedWc);
                });
                break;
            case GamePalConstants.EVENT_CODE_CURSE:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .filter(player -> !player.getBlockInfo().getId().equals(world.getEffectMap().get(eventBlock.getBlockInfo().getId())))
                        .filter(player -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                        eventBlock.getWorldCoordinate(), player.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CURSE) < 0)
                        .forEach(worldBlock ->  {
                            Block player = creatureMap.get(worldBlock.getBlockInfo().getId());
                            PlayerInfo playerInfo = world.getPlayerInfoMap().get(worldBlock.getBlockInfo().getId());
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] != -1) {
                                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] = GamePalConstants.BUFF_DEFAULT_FRAME_SAD;
                                playerService.changeVp(worldBlock.getBlockInfo().getId(), 0, true);
                            }
                        });
                break;
            case GamePalConstants.EVENT_CODE_CHEER:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .filter(player -> !player.getBlockInfo().getId().equals(world.getEffectMap().get(eventBlock.getBlockInfo().getId())))
                        .filter(player -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                        eventBlock.getWorldCoordinate(), player.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CHEER) < 0)
                        .forEach(worldBlock ->  {
                            PlayerInfo playerInfo = world.getPlayerInfoMap().get(worldBlock.getBlockInfo().getId());
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HAPPY] != -1) {
                                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HAPPY] = GamePalConstants.BUFF_DEFAULT_FRAME_HAPPY;
                                playerService.changeVp(worldBlock.getBlockInfo().getId(), playerInfo.getVpMax(), true);
                            }
                        });
                break;
            default:
                break;
        }
        addEventNoise(world, eventBlock);
    }

    private void addEventNoise(GameWorld world, final Block eventBlock) {
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block fromPlayer = creatureMap.get(world.getEffectMap().get(eventBlock.getBlockInfo().getId()));
        switch (Integer.parseInt(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
                addEvent(world, GamePalConstants.EVENT_CODE_NOISE, world.getEffectMap().get(eventBlock.getBlockInfo().getId()),
                        fromPlayer.getWorldCoordinate());
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                addEvent(world, GamePalConstants.EVENT_CODE_NOISE, world.getEffectMap().get(eventBlock.getBlockInfo().getId()),
                        eventBlock.getWorldCoordinate());
                break;
            default:
                break;
        }
    }

    private void updateBullet(GameWorld world, Block bulletBlock, List<Block> affectedBlockList) {
        Region region = world.getRegionMap().get(bulletBlock.getWorldCoordinate().getRegionNo());
        Integer eventCode = null;
        Optional<Block> targetBlock = affectedBlockList.stream()
                .filter(block -> BlockUtil.detectCollision(region, bulletBlock, block))
                .filter(block -> null != BlockUtil.calculateDistance(region, bulletBlock.getWorldCoordinate(), block.getWorldCoordinate()))
                .min(Comparator.comparing(block -> BlockUtil.calculateDistance(region, bulletBlock.getWorldCoordinate(), block.getWorldCoordinate())));
        switch (bulletBlock.getMovementInfo().getFloorCode()) {
            case BlockConstants.BLOCK_CODE_DIRT:
            case BlockConstants.BLOCK_CODE_GRASS:
                eventCode = GamePalConstants.EVENT_CODE_ASH;
            case BlockConstants.BLOCK_CODE_ROUGH:
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
                eventCode = GamePalConstants.EVENT_CODE_SPARK;
                break;
            case BlockConstants.BLOCK_CODE_LAVA:
                eventCode = GamePalConstants.EVENT_CODE_TAIL_SMOKE;
                break;
            case BlockConstants.BLOCK_CODE_SWAMP:
            case BlockConstants.BLOCK_CODE_WATER:
                eventCode = GamePalConstants.EVENT_CODE_WATER;
                break;
            case BlockConstants.BLOCK_CODE_NOTHING:
            case BlockConstants.BLOCK_CODE_SAND:
            case BlockConstants.BLOCK_CODE_SNOW:
            default:
                break;
        }
        if (targetBlock.isPresent()) {
            switch (targetBlock.get().getBlockInfo().getType()) {
                case BlockConstants.BLOCK_TYPE_PLAYER:
                    eventCode = GamePalConstants.EVENT_CODE_BLEED;
                    break;
                case BlockConstants.BLOCK_TYPE_DROP:
                case BlockConstants.BLOCK_TYPE_TELEPORT:
                case BlockConstants.BLOCK_TYPE_TREE:
                    eventCode = GamePalConstants.EVENT_CODE_ASH;
                    break;
                case BlockConstants.BLOCK_TYPE_BED:
                case BlockConstants.BLOCK_TYPE_TOILET:
                case BlockConstants.BLOCK_TYPE_DRESSER:
                case BlockConstants.BLOCK_TYPE_STORAGE:
                case BlockConstants.BLOCK_TYPE_COOKER:
                case BlockConstants.BLOCK_TYPE_SINK:
                case BlockConstants.BLOCK_TYPE_CONTAINER:
                case BlockConstants.BLOCK_TYPE_RADIO:
                case BlockConstants.BLOCK_TYPE_BUILDING:
                case BlockConstants.BLOCK_TYPE_ROCK:
                case BlockConstants.BLOCK_TYPE_WORKSHOP:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                    eventCode = GamePalConstants.EVENT_CODE_SPARK;
                    break;
                default:
                    break;
            }
        }
        if (null != eventCode) {
            addEvent(world, eventCode, world.getEffectMap().get(bulletBlock.getBlockInfo().getId()),
                    bulletBlock.getWorldCoordinate());
        }
    }

    @Override
    public void updateEvents(GameWorld world) {
        world.getBlockMap().values()
                .forEach(block -> {
                    worldService.expandByCoordinate(world, null, block.getWorldCoordinate(), 0);
                    updateEvent(world, block);
                });
    }

    private void updateEvent(GameWorld world, Block newEvent) {
        newEvent.getMovementInfo().setFrame(newEvent.getMovementInfo().getFrame() + 1);
        updateEventLocation(world, newEvent);
        if (newEvent.getMovementInfo().getFrame() >= newEvent.getMovementInfo().getPeriod()) {
            if (newEvent.getMovementInfo().getFrameMax() == -1) {
                newEvent.getMovementInfo().setFrame(newEvent.getMovementInfo().getFrame() - newEvent.getMovementInfo().getPeriod());
            } else {
                sceneManager.removeBlock(world, newEvent);
                return;
            }
        }
        String fromId = world.getEffectMap().get(newEvent.getBlockInfo().getId());
        Region region = world.getRegionMap().get(newEvent.getWorldCoordinate().getRegionNo());
        if (newEvent.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_EFFECT) {
            switch (Integer.parseInt(newEvent.getBlockInfo().getCode())) {
                case GamePalConstants.EVENT_CODE_MINE:
                    if (world.getCreatureMap().values().stream()
                            .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                            .filter(player -> !StringUtils.equals(fromId, player.getBlockInfo().getId()))
                            .anyMatch(player -> {
                                BigDecimal distance = BlockUtil.calculateDistance(
                                        world.getRegionMap().get(newEvent.getWorldCoordinate().getRegionNo()), newEvent.getWorldCoordinate(), player.getWorldCoordinate());
                                return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_MINE) < 0;
                            })) {
                        addEvent(world, GamePalConstants.EVENT_CODE_EXPLODE, fromId, newEvent.getWorldCoordinate());
                        sceneManager.removeBlock(world, newEvent);
                        return;
                    }
                    break;
                case GamePalConstants.EVENT_CODE_FIRE:
                    // Burn grid
                    if (sceneManager.getGridBlockCode(world, newEvent.getWorldCoordinate()) == BlockConstants.BLOCK_CODE_GRASS
                            || sceneManager.getGridBlockCode(world, newEvent.getWorldCoordinate()) == BlockConstants.BLOCK_CODE_SNOW) {
                        sceneManager.setGridBlockCode(world, newEvent.getWorldCoordinate(), BlockConstants.BLOCK_CODE_DIRT);
                    }
                    // Burn players
                    world.getCreatureMap().values().stream()
                            .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                            .filter(player -> {
                                BigDecimal distance = BlockUtil.calculateDistance(
                                        world.getRegionMap().get(newEvent.getWorldCoordinate().getRegionNo()), newEvent.getWorldCoordinate(), player.getWorldCoordinate());
                                return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                            })
                            .forEach(player -> {
                                affectBlock(world, newEvent, player);
//                                addEvent(world, GamePalConstants.EVENT_CODE_BLEED, player.getBlockInfo().getId(), player.getWorldCoordinate());
                            });
                    break;
                default:
                    break;
            }
        }
    }

    private void updateEventLocation(GameWorld world, Block newEvent) {
        String fromId = world.getEffectMap().get(newEvent.getBlockInfo().getId());
        if (newEvent.getBlockInfo().getCode().equals(String.valueOf(GamePalConstants.EVENT_CODE_BLOCK))
                || newEvent.getBlockInfo().getCode().equals(String.valueOf(GamePalConstants.EVENT_CODE_HEAL))
                || newEvent.getBlockInfo().getCode().equals(String.valueOf(GamePalConstants.EVENT_CODE_SACRIFICE))
                || newEvent.getBlockInfo().getCode().equals(String.valueOf(GamePalConstants.EVENT_CODE_DISTURB))) {
                // Stick with playerInfo
                BlockUtil.copyWorldCoordinate(world.getCreatureMap().get(fromId).getWorldCoordinate(),
                        newEvent.getWorldCoordinate());
        }
    }

    @Override
    public void affectBlock(GameWorld world, Block eventBlock, Block targetBlock) {
        if (eventBlock.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_EFFECT) {
            logger.error(ErrorUtil.ERROR_1013);
            return;
        }
        int changedHp = SkillUtil.calculateChangedHp(Integer.parseInt(eventBlock.getBlockInfo().getCode()),
                targetBlock.getBlockInfo().getType());
        if (targetBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                && world.getPlayerInfoMap().get(targetBlock.getBlockInfo().getId()).getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != 0) {
            changedHp /= 2;
        }
        if (targetBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                && world.getPlayerInfoMap().get(targetBlock.getBlockInfo().getId()).getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN
                && !world.getNpcBrainMap().get(targetBlock.getBlockInfo().getId()).getExemption()[CreatureConstants.NPC_EXEMPTION_ALL]) {
            npcManager.prepare2Attack(world, targetBlock.getBlockInfo().getId(), eventBlock.getBlockInfo().getCode());
        }
        changeHp(world, targetBlock, changedHp, false);
    }

    @Override
    public void changeHp(GameWorld world, Block block, int value, boolean isAbsolute) {
        Random random = new Random();
        Region region = world.getRegionMap().get(block.getWorldCoordinate().getRegionNo());
        int oldHp = block.getBlockInfo().getHp().get();
        int newHp = isAbsolute ? value : oldHp + value;
        if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER) {
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
            PlayerInfo playerInfo = playerInfoMap.get(block.getBlockInfo().getId());
            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_INVINCIBLE] != 0) {
                newHp = Math.max(oldHp, newHp);
            }
            if (newHp < oldHp) {
                WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
                        block.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                        BigDecimal.valueOf(random.nextDouble() / 2));
                addEvent(world, GamePalConstants.EVENT_CODE_BLEED, block.getBlockInfo().getId(), bleedWc);
            }
            block.getBlockInfo().getHp().set(Math.max(0, Math.min(newHp, block.getBlockInfo().getHpMax().get())));
            if (block.getBlockInfo().getHp().get() <= 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0) {
                playerService.killPlayer(block.getBlockInfo().getId());
            }
        } else {
            block.getBlockInfo().getHp().set(Math.max(0, Math.min(newHp, block.getBlockInfo().getHpMax().get())));
            if (block.getBlockInfo().getHp().get() <= 0) {
                addEvent(world, GamePalConstants.EVENT_CODE_TAIL_SMOKE, block.getBlockInfo().getId(), block.getWorldCoordinate());
                sceneManager.removeBlock(world, block);
            }
        }
    }
}
