package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.EventInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
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

    @Override
    public MovementInfo createMovementInfo(final int eventCode) {
        MovementInfo movementInfo = new MovementInfo();
        movementInfo.setFrame(0);
        switch (eventCode) {
            case GamePalConstants.EVENT_CODE_MINE:
                // Infinite
                movementInfo.setFrameMax(-1);
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
        MovementInfo movementInfo = createMovementInfo(eventCode);
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
        Map<String, Block> creatureMap = world.getCreatureMap();
        String fromId = world.getEffectMap().get(eventBlock.getBlockInfo().getId());
        Block fromPlayer = world.getCreatureMap().get(fromId);
        int changedHp = SkillUtil.calculateChangedHp(Integer.parseInt(eventBlock.getBlockInfo().getCode()));
        // Show itself
        switch (Integer.parseInt(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                addEvent(world, GamePalConstants.EVENT_CODE_EXPLODE, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), eventBlock.getWorldCoordinate());
                break;
            default:
                world.getEventQueue().add(eventBlock);
                break;
        }
        // Effect after activation
        switch (Integer.parseInt(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_HEAL:
                playerService.changeHp(world.getEffectMap().get(eventBlock.getBlockInfo().getId()), changedHp, false);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            PlayerInfo playerInfo = world.getPlayerInfoMap().get(player.getBlockInfo().getId());
                            int damageValue = changedHp;
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != 0) {
                                damageValue /= 2;
                            }
                            playerService.damageHp(player.getBlockInfo().getId(), world.getEffectMap().get(eventBlock.getBlockInfo().getId()),
                                    damageValue, false);
                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                    world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                                    BigDecimal.valueOf(random.nextDouble() / 2));
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), bleedWc);
                });
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            playerService.damageHp(player.getBlockInfo().getId(), world.getEffectMap().get(eventBlock.getBlockInfo().getId()),
                                    changedHp, false);
                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                    world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                                    BigDecimal.valueOf(random.nextDouble() / 2));
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), bleedWc);
                });
                WorldCoordinate sparkWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        world.getRegionMap().get(fromPlayer.getWorldCoordinate().getRegionNo()),
                        fromPlayer.getWorldCoordinate(),
                        fromPlayer.getMovementInfo().getFaceDirection().add(BigDecimal.valueOf(
                                SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue() * 2
                                        * (random.nextDouble() - 0.5D))), BigDecimal.ONE);
                addEvent(world, GamePalConstants.EVENT_CODE_SPARK, world.getEffectMap().get(eventBlock.getBlockInfo().getId()), sparkWc);
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
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(player -> {
                            playerService.damageHp(player.getBlockInfo().getId(), world.getEffectMap().get(eventBlock.getBlockInfo().getId()),
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
                            Block player = creatureMap.get(worldBlock.getBlockInfo().getId());
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

    @Override
    public void updateEvents(GameWorld world) {
        world.getBlockMap().values()
                .forEach(block -> {
                    updateEvent(world, block);
                    worldService.expandByCoordinate(world, null, block.getWorldCoordinate(), 0);
                });
    }

//    public void updateEventsOld(GameWorld world) {
//        Map<Integer, Region> regionMap = world.getRegionMap();
//        // Clear events from scene 24/08/07
//        regionMap.values().stream()
//                .filter(region -> null != region.getScenes())
//                .filter(region -> !region.getScenes().isEmpty())
//                .forEach(region -> region.getScenes().values()
//                        .forEach(scene -> scene.getEvents().clear())
//                );
//        Queue<Block> eventQueue = world.getEventQueue();
//        Block tailEvent = new Block(new WorldCoordinate(), new BlockInfo(BlockConstants.BLOCK_TYPE_NORMAL, "", "1001",
//                new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID, BlockConstants.STRUCTURE_LAYER_MIDDLE)),
//                new MovementInfo(), new PlayerInfo(), new EventInfo());
//        eventQueue.add(tailEvent);
//        while (tailEvent != eventQueue.peek()) {
//            Block newEvent = updateEvent(world, eventQueue.poll());
//            if (null != newEvent) {
//                eventQueue.add(newEvent);
//                worldService.expandByCoordinate(world, null, newEvent.getWorldCoordinate(), 0);
//                regionMap.get(newEvent.getWorldCoordinate().getRegionNo()).getScenes()
//                        .get(newEvent.getWorldCoordinate().getSceneCoordinate()).getEvents()
//                        .add(newEvent);
//            }
//        }
//        eventQueue.poll();
//    }

    private Block updateEvent(GameWorld world, Block oldEvent) {
        Block newEvent = new Block(oldEvent);
        newEvent.getMovementInfo().setFrame(newEvent.getMovementInfo().getFrame() + 1);
        updateEventLocation(world, newEvent);
        if (newEvent.getMovementInfo().getFrame() >= newEvent.getMovementInfo().getPeriod()) {
            if (newEvent.getMovementInfo().getFrameMax() == -1) {
                newEvent.getMovementInfo().setFrame(newEvent.getMovementInfo().getFrame() - newEvent.getMovementInfo().getPeriod());
            } else {
                sceneManager.removeBlock(world, newEvent);
            }
        }
        String fromId = world.getEffectMap().get(newEvent.getBlockInfo().getId());
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
                    return null;
                }
                break;
            case GamePalConstants.EVENT_CODE_FIRE:
                // Extinguished by water
                if (world.getEventQueue().stream()
                        .filter(worldEvent -> Integer.parseInt(worldEvent.getBlockInfo().getCode()) == GamePalConstants.EVENT_CODE_WATER)
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
                            playerService.damageHp(player.getBlockInfo().getId(), fromId,
                                    SkillUtil.calculateChangedHp(Integer.parseInt(newEvent.getBlockInfo().getCode())), false);
                            addEvent(world, GamePalConstants.EVENT_CODE_BLEED, player.getBlockInfo().getId(), player.getWorldCoordinate());
                        });
                break;
            default:
                break;
        }
        return newEvent;
    }

    private void updateEventLocation(GameWorld world, Block newEvent) {
        String fromId = world.getEffectMap().get(newEvent.getBlockInfo().getId());
        switch (Integer.parseInt(newEvent.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_SACRIFICE:
            case GamePalConstants.EVENT_CODE_DISTURB:
                // Stick with playerInfo
                BlockUtil.copyWorldCoordinate(world.getCreatureMap().get(fromId).getWorldCoordinate(),
                        newEvent.getWorldCoordinate());
                break;
            default:
                // Keep its position
                break;
        }
    }
}
