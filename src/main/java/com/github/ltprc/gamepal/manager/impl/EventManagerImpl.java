package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.EventInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class EventManagerImpl implements EventManager {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private SceneManager sceneManager;

    @Override
    public EventInfo createEventInfo(final int eventCode, final String eventId) {
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEventCode(eventCode);
        eventInfo.setEventId(eventId);
        eventInfo.setFrame(0);
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_MINE:
                // Infinite
                eventInfo.setFrameMax(-1);
                break;
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                eventInfo.setFrameMax(50);
                break;
            case GamePalConstants.EVENT_CODE_FIRE:
                eventInfo.setFrameMax(250);
                break;
            default:
                eventInfo.setFrameMax(25);
                break;
        }
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_DISTURB:
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                eventInfo.setPeriod(50);
                break;
            default:
                eventInfo.setPeriod(25);
                break;
        }
        return eventInfo;
    }

//    @Override
//    public Block createEventBlock(WorldCoordinate worldCoordinate, EventInfo eventInfo) {
//        int structureMaterial;
//        switch (eventInfo.getEventCode()) {
//            case GamePalConstants.EVENT_CODE_MELEE_HIT:
//            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
//            case GamePalConstants.EVENT_CODE_MELEE_KICK:
//            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
//            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
//            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
//                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_SOLID;
//                break;
//            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
//            case GamePalConstants.EVENT_CODE_MELEE_STAB:
//            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
//            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
//                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_MAGNUM;
//                break;
//            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
//            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
//                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_PLASMA;
//                break;
//            default:
//                structureMaterial = BlockConstants.STRUCTURE_MATERIAL_HOLLOW;
//                break;
//        }
//        return new Block(worldCoordinate, new BlockInfo(BlockConstants.BLOCK_TYPE_EVENT, "", "",
//                new Structure(structureMaterial, BlockUtil.convertEventCode2Layer(eventInfo.getEventCode()),
//                        new Shape(BlockConstants.STRUCTURE_SHAPE_TYPE_ROUND,
//                                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
//                                new Coordinate(BlockConstants.EVENT_RADIUS, BlockConstants.EVENT_RADIUS)))),
//                new MovementInfo(), new PlayerInfo(), eventInfo);
//    }

    @Override
    public void addEvent(GameWorld world, int eventCode, String eventId, WorldCoordinate worldCoordinate) {
        EventInfo eventInfo = createEventInfo(eventCode, eventId);
        Block eventBlock = sceneManager.addEventBlock(world, eventInfo, worldCoordinate);
        correctTarget(world, eventBlock);
        List<Block> affectedBlockList = collectAffectedBlocks(world, eventBlock);
        activateEvent(world, eventBlock, affectedBlockList);
    }

    private void correctTarget(GameWorld world, Block eventBlock) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        Block fromCreature = world.getCreatureMap().get(eventBlock.getEventInfo().getEventId());
        WorldCoordinate from = fromCreature.getWorldCoordinate();
        Region region = regionMap.get(from.getRegionNo());
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        Set<IntegerCoordinate> preSelectedSceneCoordinates =
                BlockUtil.preSelectSceneCoordinates(region, from, worldCoordinate);
        // Detect the maximum moving distance
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
                region.getScenes().get(sceneCoordinate).getBlocks().stream()
                        .filter(blocker -> BlockUtil.checkMaterialCollision(
                                eventBlock.getBlockInfo().getStructure().getMaterial(),
                                blocker.getBlockInfo().getStructure().getMaterial()))
                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
                                BlockUtil.calculateAngle(region, from, eventBlock.getWorldCoordinate()).doubleValue()) < 135D)
//                        .filter(blocker -> {
//                            return BlockUtil.compareAnglesInDegrees(
//                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
//                                fromCreature.getMovementInfo().getFaceDirection().doubleValue()) < 135D;
//                        })
                        .forEach(blocker ->
                            BlockUtil.detectLineCollision(region, from, eventBlock, blocker, true)
                        ));
    }

    private List<Block> collectAffectedBlocks(GameWorld world, Block eventBlock) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        Block fromCreature = world.getCreatureMap().get(eventBlock.getEventInfo().getEventId());
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
                region.getScenes().get(sceneCoordinate).getBlocks().stream()
                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
                                BlockUtil.calculateAngle(region, from, eventBlock.getWorldCoordinate()).doubleValue()) < 135D)
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
        switch (eventBlock.getEventInfo().getEventCode()) {
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
                rst = !eventBlock.getEventInfo().getEventId().equals(blocker.getBlockInfo().getId())
                        && distance.compareTo(SkillConstants.SKILL_RANGE_MELEE) <= 0
                        && deltaAngle < SkillConstants.SKILL_ANGLE_MELEE_MAX.doubleValue();
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                rst = !eventBlock.getEventInfo().getEventId().equals(blocker.getBlockInfo().getId())
                        && distance.compareTo(SkillConstants.SKILL_RANGE_SHOOT) <= 0
                        && deltaAngle < SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                        && BlockUtil.detectLineCollision(region, from, eventBlock, blocker, false);
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                rst = distance.compareTo(SkillConstants.SKILL_RANGE_EXPLODE) <= 0;
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block fromPlayer = creatureMap.get(eventBlock.getEventInfo().getEventId());
        int changedHp = SkillUtil.calculateChangedHp(eventBlock.getEventInfo().getEventCode());
        // Show itself
        switch (eventBlock.getEventInfo().getEventCode()) {
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                addEvent(world, GamePalConstants.EVENT_CODE_EXPLODE, eventBlock.getEventInfo().getEventId(), eventBlock.getWorldCoordinate());
                break;
            default:
                world.getEventQueue().add(eventBlock);
                break;
        }
        // Effect after activation
        switch (eventBlock.getEventInfo().getEventCode()) {
            case GamePalConstants.EVENT_CODE_HEAL:
                playerService.changeHp(eventBlock.getEventInfo().getEventId(), changedHp, false);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            PlayerInfo playerInfo = player.getPlayerInfo();
                            int damageValue = changedHp;
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != 0) {
                                damageValue /= 2;
                            }
                            playerService.damageHp(player.getBlockInfo().getId(), eventBlock.getEventInfo().getEventId(),
                                    damageValue, false);
                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                    world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                                    BigDecimal.valueOf(random.nextDouble() / 2));
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, eventBlock.getEventInfo().getEventId(), bleedWc);
                });
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            playerService.damageHp(player.getBlockInfo().getId(), eventBlock.getEventInfo().getEventId(),
                                    changedHp, false);
                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                    world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                                    BigDecimal.valueOf(random.nextDouble() / 2));
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, eventBlock.getEventInfo().getEventId(), bleedWc);
                });
                WorldCoordinate sparkWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        world.getRegionMap().get(fromPlayer.getWorldCoordinate().getRegionNo()),
                        fromPlayer.getWorldCoordinate(),
                        fromPlayer.getMovementInfo().getFaceDirection().add(BigDecimal.valueOf(
                                SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue() * 2
                                        * (random.nextDouble() - 0.5D))), BigDecimal.ONE);
                addEvent(world, GamePalConstants.EVENT_CODE_SPARK, eventBlock.getEventInfo().getEventId(), sparkWc);
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                BigDecimal tailSmokeLength = BlockUtil.calculateDistance(regionMap.get(
                        eventBlock.getWorldCoordinate().getRegionNo()),
                        creatureMap.get(eventBlock.getEventInfo().getEventId()).getWorldCoordinate(),
                        eventBlock.getWorldCoordinate());
                if (null != tailSmokeLength) {
                    int tailSmokeAmount = tailSmokeLength.intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                            creatureMap.get(eventBlock.getEventInfo().getEventId()).getWorldCoordinate(),
                            eventBlock.getWorldCoordinate(), tailSmokeAmount);
                    equidistantPoints.stream()
                            .forEach(tailSmokeCoordinate -> {
//                                BlockUtil.fixWorldCoordinate(regionMap.get(
//                                        eventBlock.getWorldCoordinate().getRegionNo()), tailSmokeCoordinate);
                                addEvent(world, GamePalConstants.EVENT_CODE_TAIL_SMOKE, eventBlock.getEventInfo().getEventId(), tailSmokeCoordinate);
                            });
                }
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
                BigDecimal flameLength = BlockUtil.calculateDistance(regionMap.get(
                                eventBlock.getWorldCoordinate().getRegionNo()),
                        creatureMap.get(eventBlock.getEventInfo().getEventId()).getWorldCoordinate(),
                        eventBlock.getWorldCoordinate());
                if (null != flameLength) {
                    int flameAmount = flameLength.subtract(SkillConstants.SKILL_RANGE_SHOOT_FIRE_MIN).max(BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(2)).intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                            creatureMap.get(eventBlock.getEventInfo().getEventId()).getWorldCoordinate(),
                            eventBlock.getWorldCoordinate(), flameAmount);
                    equidistantPoints.stream()
                            .forEach(flameCoordinate -> {
//                                BlockUtil.fixWorldCoordinate(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
//                                        flameCoordinate);
                                addEvent(world, GamePalConstants.EVENT_CODE_FIRE, eventBlock.getEventInfo().getEventId(), flameCoordinate);
                            });
                }
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
                addEvent(world, GamePalConstants.EVENT_CODE_WATER, eventBlock.getEventInfo().getEventId(), eventBlock.getWorldCoordinate());
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            playerService.damageHp(player.getBlockInfo().getId(), eventBlock.getEventInfo().getEventId(),
                                    changedHp, false);
                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                    world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                                    BigDecimal.valueOf(random.nextDouble() / 2));
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, player.getBlockInfo().getId(), bleedWc);
                });
                break;
            case GamePalConstants.EVENT_CODE_CURSE:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .filter(player -> !player.getBlockInfo().getId().equals(eventBlock.getEventInfo().getEventId()))
                        .filter(player -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                        eventBlock.getWorldCoordinate(), player.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CURSE) < 0)
                        .forEach(worldBlock ->  {
                            Block player = creatureMap.get(worldBlock.getBlockInfo().getId());
                            PlayerInfo playerInfo = player.getPlayerInfo();
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] != -1) {
                                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] = GamePalConstants.BUFF_DEFAULT_FRAME_SAD;
                                playerService.changeVp(worldBlock.getBlockInfo().getId(), 0, true);
                            }
                        });
                break;
            case GamePalConstants.EVENT_CODE_CHEER:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .filter(player -> !player.getBlockInfo().getId().equals(eventBlock.getEventInfo().getEventId()))
                        .filter(player -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                        eventBlock.getWorldCoordinate(), player.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CHEER) < 0)
                        .forEach(worldBlock ->  {
                            Block player = creatureMap.get(worldBlock.getBlockInfo().getId());
                            PlayerInfo playerInfo = player.getPlayerInfo();
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
        Block fromPlayer = creatureMap.get(eventBlock.getEventInfo().getEventId());
        switch (eventBlock.getEventInfo().getEventCode()) {
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
                addEvent(world, GamePalConstants.EVENT_CODE_NOISE, eventBlock.getEventInfo().getEventId(),
                        fromPlayer.getWorldCoordinate());
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                addEvent(world, GamePalConstants.EVENT_CODE_NOISE, eventBlock.getEventInfo().getEventId(),
                        eventBlock.getWorldCoordinate());
                break;
            default:
                break;
        }
    }

    @Override
    public void updateEvents(GameWorld world) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        // Clear events from scene 24/08/07
        regionMap.values().stream()
                .filter(region -> null != region.getScenes())
                .filter(region -> !region.getScenes().isEmpty())
                .forEach(region -> region.getScenes().values()
                        .forEach(scene -> scene.getEvents().clear())
        );
        Queue<Block> eventQueue = world.getEventQueue();
        Block tailEvent = new Block(new WorldCoordinate(), new BlockInfo(BlockConstants.BLOCK_TYPE_EVENT, "", "",
                new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE)),
                new MovementInfo(), new PlayerInfo(), new EventInfo());
        eventQueue.add(tailEvent);
        while (tailEvent != eventQueue.peek()) {
            Block newEvent = updateEvent(world, eventQueue.poll());
            if (null != newEvent) {
                eventQueue.add(newEvent);
                worldService.expandByCoordinate(world, null, newEvent.getWorldCoordinate(), 0);
                regionMap.get(newEvent.getWorldCoordinate().getRegionNo()).getScenes()
                        .get(newEvent.getWorldCoordinate().getSceneCoordinate()).getEvents()
                        .add(newEvent);
            }
        }
        eventQueue.poll();
    }

    private Block updateEvent(GameWorld world, Block oldEvent) {
        Block newEvent = new Block(oldEvent);
        newEvent.getEventInfo().setFrame(newEvent.getEventInfo().getFrame() + 1);
        updateEventLocation(world, newEvent);
        if (newEvent.getEventInfo().getFrame() >= newEvent.getEventInfo().getPeriod()) {
            if (newEvent.getEventInfo().getFrameMax() == -1) {
                newEvent.getEventInfo().setFrame(newEvent.getEventInfo().getFrame() - newEvent.getEventInfo().getPeriod());
            } else {
                return null;
            }
        }
        switch (newEvent.getEventInfo().getEventCode()) {
            case GamePalConstants.EVENT_CODE_MINE:
                if (world.getCreatureMap().values().stream()
                        .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                        .filter(player -> !StringUtils.equals(newEvent.getEventInfo().getEventId(), player.getBlockInfo().getId()))
                        .anyMatch(player -> {
                            BigDecimal distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(newEvent.getWorldCoordinate().getRegionNo()), newEvent.getWorldCoordinate(), player.getWorldCoordinate());
                            return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_MINE) < 0;
                        })) {
                    addEvent(world, GamePalConstants.EVENT_CODE_EXPLODE, newEvent.getEventInfo().getEventId(), newEvent.getWorldCoordinate());
                    return null;
                }
                break;
            case GamePalConstants.EVENT_CODE_FIRE:
                // Extinguished by water
                if (world.getEventQueue().stream()
                        .filter(worldEvent -> worldEvent.getEventInfo().getEventCode() == GamePalConstants.EVENT_CODE_WATER)
                        .anyMatch(worldEvent -> {
                            BigDecimal distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(newEvent.getWorldCoordinate().getRegionNo()), newEvent.getWorldCoordinate(), worldEvent.getWorldCoordinate());
                            return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                        })) {
                    return null;
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
                            playerService.damageHp(player.getBlockInfo().getId(), newEvent.getEventInfo().getEventId(),
                                    SkillUtil.calculateChangedHp(newEvent.getEventInfo().getEventCode()), false);
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, player.getBlockInfo().getId(), player.getWorldCoordinate());
                        });
                break;
            default:
                break;
        }
        return newEvent;
    }

    private void updateEventLocation(GameWorld world, Block newEvent) {
        switch (newEvent.getEventInfo().getEventCode()) {
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_SACRIFICE:
            case GamePalConstants.EVENT_CODE_DISTURB:
                // Stick with playerInfo
                BlockUtil.copyWorldCoordinate(world.getCreatureMap().get(newEvent.getEventInfo().getEventId()).getWorldCoordinate(),
                        newEvent.getWorldCoordinate());
                break;
            default:
                // Keep its position
                break;
        }
    }
}
