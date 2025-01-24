package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class EventManagerImpl implements EventManager {

    private static final Log logger = LogFactory.getLog(EventManagerImpl.class);

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private MovementManager movementManager;

    @Override
    public void addEvent(GameWorld world, int eventCode, String sourceId, WorldCoordinate worldCoordinate) {
        BlockInfo blockInfo = BlockUtil.createBlockInfoByCode(eventCode);
        MovementInfo movementInfo = BlockUtil.createMovementInfoByEventCode(eventCode);
        Block eventBlock = sceneManager.addOtherBlock(world, worldCoordinate, blockInfo, movementInfo);
        if (StringUtils.isNotBlank(sourceId)) {
            world.getSourceMap().put(eventBlock.getBlockInfo().getId(), sourceId);
        }
        Block fromCreature = world.getCreatureMap().getOrDefault(sourceId, eventBlock);
        correctTarget(world, eventBlock, fromCreature);
        List<Block> affectedBlockList = collectAffectedBlocks(world, eventBlock, fromCreature);
        activateEvent(world, eventBlock, fromCreature, affectedBlockList);
    }

    private void correctTarget(GameWorld world, Block eventBlock, Block fromCreature) {
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        WorldCoordinate fromWorldCoordinate = fromCreature.getWorldCoordinate();
        if (worldCoordinate.getRegionNo() != fromWorldCoordinate.getRegionNo()) {
            return;
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        Set<IntegerCoordinate> preSelectedSceneCoordinates =
                BlockUtil.preSelectSceneCoordinates(region, fromWorldCoordinate, worldCoordinate);
        // Detect the maximum moving distance
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
                region.getScenes().get(sceneCoordinate).getBlocks().values().stream()
                        .filter(blocker -> region.getRegionNo() == blocker.getWorldCoordinate().getRegionNo())
                        .filter(blocker -> BlockUtil.checkMaterialCollision(
                                eventBlock.getBlockInfo().getStructure().getMaterial(),
                                blocker.getBlockInfo().getStructure().getMaterial()))
                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, fromWorldCoordinate, blocker.getWorldCoordinate()).doubleValue(),
                                BlockUtil.calculateAngle(region, fromWorldCoordinate, worldCoordinate).doubleValue()) < 135D)
                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, fromWorldCoordinate, blocker.getWorldCoordinate()).doubleValue(),
                                fromCreature.getMovementInfo().getFaceDirection().doubleValue()) < 135D
                        )
                        .forEach(blocker ->
                                BlockUtil.detectLineCollision(region, fromWorldCoordinate, eventBlock, blocker, true)
                        ));
    }

    private List<Block> collectAffectedBlocks(GameWorld world, Block eventBlock, Block fromCreature) {
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        WorldCoordinate fromWorldCoordinate = fromCreature.getWorldCoordinate();
        if (worldCoordinate.getRegionNo() != fromWorldCoordinate.getRegionNo()) {
            return new ArrayList<>();
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        Set<IntegerCoordinate> preSelectedSceneCoordinates =
                BlockUtil.preSelectSceneCoordinates(region, fromWorldCoordinate, worldCoordinate);
        // Pre-select blocks including creatures
        List<Block> preSelectedBlocks = world.getCreatureMap().values().stream()
                .filter(creature -> creature.getWorldCoordinate().getRegionNo() == region.getRegionNo())
                .filter(creature -> preSelectedSceneCoordinates.contains(creature.getWorldCoordinate().getSceneCoordinate()))
                .filter(creature -> playerService.validateActiveness(world, creature.getBlockInfo().getId()))
//                .filter(creature -> BlockUtil.checkMaterialCollision(
//                        eventBlock.getBlockInfo().getStructure().getMaterial(),
//                        creature.getBlockInfo().getStructure().getMaterial()))
                .filter(creature -> checkEventCondition(world, fromWorldCoordinate, eventBlock, creature))
                .collect(Collectors.toList());
        // Collect all collided blocks
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
                region.getScenes().get(sceneCoordinate).getBlocks().values().stream()
//                        .filter(blocker -> BlockUtil.compareAnglesInDegrees(
//                                BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate()).doubleValue(),
//                                BlockUtil.calculateAngle(region, from, worldCoordinate).doubleValue()) < 135D)
                        .filter(blocker -> checkEventCondition(world, fromWorldCoordinate, eventBlock, blocker))
                        .forEach(preSelectedBlocks::add));
//        return shortenPreSelectedBlocks(region, fromWorldCoordinate, eventBlock, preSelectedBlocks);
        return preSelectedBlocks;
    }

    private boolean checkEventCondition(GameWorld world, WorldCoordinate from, Block eventBlock, Block blocker) {
        boolean rst = false;
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(from.getRegionNo());
        BigDecimal eventDistance = BlockUtil.calculateDistance(region, eventBlock.getWorldCoordinate(),
                blocker.getWorldCoordinate());
        BigDecimal fromDistance = BlockUtil.calculateDistance(region, from, blocker.getWorldCoordinate());
        BigDecimal angle1 = BlockUtil.calculateAngle(region, from, eventBlock.getWorldCoordinate());
        BigDecimal angle2 = BlockUtil.calculateAngle(region, from, blocker.getWorldCoordinate());
        switch (eventBlock.getBlockInfo().getCode()) {
            case BlockConstants.BLOCK_CODE_MELEE_HIT:
            case BlockConstants.BLOCK_CODE_MELEE_KICK:
            case BlockConstants.BLOCK_CODE_MELEE_SMASH:
            case BlockConstants.BLOCK_CODE_MELEE_SCRATCH:
            case BlockConstants.BLOCK_CODE_MELEE_CLEAVE:
            case BlockConstants.BLOCK_CODE_MELEE_CHOP:
            case BlockConstants.BLOCK_CODE_MELEE_PICK:
            case BlockConstants.BLOCK_CODE_MELEE_STAB:
                rst = !blocker.getBlockInfo().getId().equals(world.getSourceMap().get(eventBlock.getBlockInfo().getId()))
                        && null != eventDistance
                        && eventDistance.compareTo(SkillConstants.SKILL_RANGE_MELEE) <= 0
                        && null != angle1
                        && null != angle2
                        && BlockUtil.compareAnglesInDegrees(angle1.doubleValue(), angle2.doubleValue())
                        < SkillConstants.SKILL_ANGLE_MELEE_MAX.doubleValue();
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_HIT:
            case BlockConstants.BLOCK_CODE_SHOOT_ARROW:
            case BlockConstants.BLOCK_CODE_SHOOT_SLUG:
            case BlockConstants.BLOCK_CODE_SHOOT_MAGNUM:
            case BlockConstants.BLOCK_CODE_SHOOT_ROCKET:
                rst = !blocker.getBlockInfo().getId().equals(world.getSourceMap().get(eventBlock.getBlockInfo().getId()))
                        && null != fromDistance
                        && fromDistance.compareTo(SkillConstants.SKILL_RANGE_SHOOT) <= 0
                        && null != angle1
                        && null != angle2
                        && BlockUtil.compareAnglesInDegrees(angle1.doubleValue(), angle2.doubleValue())
                        < SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue()
                        && BlockUtil.detectLineCollision(region, from, eventBlock, blocker, false);
                break;
            case BlockConstants.BLOCK_CODE_EXPLODE:
                rst = null != eventDistance
                        && eventDistance.compareTo(SkillConstants.SKILL_RANGE_EXPLODE) <= 0;
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
                        eventBlock.getBlockInfo().getStructure().getMaterial(),
                        preSelectedBlock.getBlockInfo().getStructure().getMaterial()))
                .filter(block -> null != BlockUtil.calculateDistance(regionInfo, from, block.getWorldCoordinate()))
                .min(Comparator.comparing(block -> BlockUtil.calculateDistance(regionInfo, from, block.getWorldCoordinate())));
        List<Block> shortenedBlocks = preSelectedBlocks.stream()
                .filter(preSelectedBlock -> !BlockUtil.checkMaterialCollision(
                        eventBlock.getBlockInfo().getStructure().getMaterial(),
                        preSelectedBlock.getBlockInfo().getStructure().getMaterial()))
                .collect(Collectors.toList());
        collidedBlock.ifPresent(shortenedBlocks::add);
        return shortenedBlocks;
    }

    private void activateEvent(GameWorld world, Block eventBlock, Block fromCreature, List<Block> affectedBlockList) {
        Random random = new Random();
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        WorldCoordinate fromWorldCoordinate = fromCreature.getWorldCoordinate();
        if (worldCoordinate.getRegionNo() != fromWorldCoordinate.getRegionNo()) {
            return;
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        // Effect after activation
        switch (eventBlock.getBlockInfo().getCode()) {
            case BlockConstants.BLOCK_CODE_HEAL:
                affectBlock(world, eventBlock, fromCreature);
                break;
            case BlockConstants.BLOCK_CODE_MELEE_HIT:
            case BlockConstants.BLOCK_CODE_MELEE_KICK:
            case BlockConstants.BLOCK_CODE_MELEE_SCRATCH:
            case BlockConstants.BLOCK_CODE_MELEE_SMASH:
            case BlockConstants.BLOCK_CODE_MELEE_CLEAVE:
            case BlockConstants.BLOCK_CODE_MELEE_CHOP:
            case BlockConstants.BLOCK_CODE_MELEE_PICK:
            case BlockConstants.BLOCK_CODE_MELEE_STAB:
                affectedBlockList.forEach(target -> affectBlock(world, eventBlock, target));
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_HIT:
            case BlockConstants.BLOCK_CODE_SHOOT_ARROW:
            case BlockConstants.BLOCK_CODE_SHOOT_SLUG:
            case BlockConstants.BLOCK_CODE_SHOOT_MAGNUM:
                affectedBlockList.forEach(target -> affectBlock(world, eventBlock, target));
                updateBullet(world, eventBlock, fromCreature, affectedBlockList);
                WorldCoordinate sparkWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        world.getRegionMap().get(fromCreature.getWorldCoordinate().getRegionNo()),
                        fromCreature.getWorldCoordinate(),
                        fromCreature.getMovementInfo().getFaceDirection().add(BigDecimal.valueOf(
                                SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue() * 2
                                        * (random.nextDouble() - 0.5D))), BigDecimal.ONE);
                addEvent(world, BlockConstants.BLOCK_CODE_SPARK_SHORT, fromCreature.getBlockInfo().getId(), sparkWc);
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_ROCKET:
                BigDecimal tailSmokeLength = BlockUtil.calculateDistance(regionMap.get(
                        worldCoordinate.getRegionNo()), fromWorldCoordinate, worldCoordinate);
                if (null != tailSmokeLength) {
                    int tailSmokeAmount = tailSmokeLength.intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(worldCoordinate.getRegionNo()), fromWorldCoordinate,
                            worldCoordinate, tailSmokeAmount);
                    equidistantPoints.forEach(tailSmokeCoordinate ->
                            addEvent(world, BlockConstants.BLOCK_CODE_TAIL_SMOKE, fromCreature.getBlockInfo().getId(), tailSmokeCoordinate));
                }
                updateBullet(world, eventBlock, fromCreature, affectedBlockList);
                addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, fromCreature.getBlockInfo().getId(), worldCoordinate);
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_FIRE:
                BigDecimal flameLength = BlockUtil.calculateDistance(regionMap.get(
                        worldCoordinate.getRegionNo()), fromWorldCoordinate, worldCoordinate);
                if (null != flameLength) {
                    int flameAmount = flameLength.subtract(SkillConstants.SKILL_RANGE_SHOOT_FIRE_MIN).max(BigDecimal.ONE)
                            .multiply(BigDecimal.valueOf(2)).intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(worldCoordinate.getRegionNo()), fromWorldCoordinate,
                            worldCoordinate, flameAmount);
                    equidistantPoints.stream()
                            .forEach(flameCoordinate -> {
//                                BlockUtil.fixWorldCoordinate(regionMap.get(worldCoordinate.getRegionNo()),
//                                        flameCoordinate);
                                addEvent(world, BlockConstants.BLOCK_CODE_FIRE, fromCreature.getBlockInfo().getId(), flameCoordinate);
                            });
                }
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_SPRAY:
                addEvent(world, BlockConstants.BLOCK_CODE_SPRAY, fromCreature.getBlockInfo().getId(), worldCoordinate);
                // Extinguish fire
                region.getScenes().values().stream()
                        .filter(scene -> SkillUtil.isSceneDetected(eventBlock, scene.getSceneCoordinate(), 1))
                        .forEach(scene -> scene.getBlocks().values().stream()
                                .filter(block -> block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_EFFECT)
                                .filter(block -> block.getBlockInfo().getCode() == BlockConstants.BLOCK_CODE_FIRE)
                                .filter(block -> {
                                    BigDecimal distance = BlockUtil.calculateDistance(
                                            world.getRegionMap().get(worldCoordinate.getRegionNo()),
                                            worldCoordinate, block.getWorldCoordinate());
                                    return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                                })
                                .forEach(block -> sceneManager.removeBlock(world, block, true)));
                break;
            case BlockConstants.BLOCK_CODE_EXPLODE:
                sceneManager.setGridBlockCode(world, worldCoordinate, BlockConstants.BLOCK_CODE_LAVA);
                affectedBlockList.stream()
//                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(block -> {
                            affectBlock(world, eventBlock, block);
//                            WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(region,
//                                    player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
//                                    BigDecimal.valueOf(random.nextDouble() / 2));
//                            addEvent(world, BlockConstants.BLOCK_CODE_BLEED, player.getBlockInfo().getId(), bleedWc);
                });
                break;
            case BlockConstants.BLOCK_CODE_CURSE:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .filter(target -> !target.getBlockInfo().getId().equals(world.getSourceMap().get(eventBlock.getBlockInfo().getId())))
                        .filter(target -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(worldCoordinate.getRegionNo()),
                                        worldCoordinate, target.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CURSE) < 0)
                        .forEach(worldBlock ->  {
                            PlayerInfo playerInfo = world.getPlayerInfoMap().get(worldBlock.getBlockInfo().getId());
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] != -1) {
                                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] = GamePalConstants.BUFF_DEFAULT_FRAME_SAD;
                                playerService.changeVp(worldBlock.getBlockInfo().getId(), 0, true);
                            }
                        });
                break;
            case BlockConstants.BLOCK_CODE_CHEER:
                affectedBlockList.stream()
                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .filter(target -> !target.getBlockInfo().getId().equals(world.getSourceMap().get(eventBlock.getBlockInfo().getId())))
                        .filter(target -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(worldCoordinate.getRegionNo()),
                                        worldCoordinate, target.getWorldCoordinate())
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
        addEventNoise(world, eventBlock, fromCreature);
    }

    private void addEventNoise(GameWorld world, Block eventBlock, Block fromCreature) {
        switch (eventBlock.getBlockInfo().getCode()) {
            case BlockConstants.BLOCK_CODE_SHOOT_SLUG:
            case BlockConstants.BLOCK_CODE_SHOOT_MAGNUM:
            case BlockConstants.BLOCK_CODE_SHOOT_ROCKET:
            case BlockConstants.BLOCK_CODE_SHOOT_FIRE:
            case BlockConstants.BLOCK_CODE_SHOOT_SPRAY:
                addEvent(world, BlockConstants.BLOCK_CODE_NOISE, fromCreature.getBlockInfo().getId(), fromCreature.getWorldCoordinate());
                break;
            case BlockConstants.BLOCK_CODE_EXPLODE:
                addEvent(world, BlockConstants.BLOCK_CODE_NOISE, fromCreature.getBlockInfo().getId(), eventBlock.getWorldCoordinate());
                break;
            default:
                break;
        }
    }

    private void updateBullet(GameWorld world, Block bulletBlock, Block fromCreature, List<Block> affectedBlockList) {
        Region region = world.getRegionMap().get(bulletBlock.getWorldCoordinate().getRegionNo());
        Integer eventCode = null;
        Optional<Block> targetBlock = affectedBlockList.stream()
                .filter(block -> BlockUtil.detectCollision(region, bulletBlock, block))
                .filter(block -> null != BlockUtil.calculateDistance(region, bulletBlock.getWorldCoordinate(), block.getWorldCoordinate()))
                .min(Comparator.comparing(block -> BlockUtil.calculateDistance(region, bulletBlock.getWorldCoordinate(), block.getWorldCoordinate())));
        switch (bulletBlock.getMovementInfo().getFloorCode()) {
            case BlockConstants.BLOCK_CODE_DIRT:
            case BlockConstants.BLOCK_CODE_GRASS:
                eventCode = BlockConstants.BLOCK_CODE_LIGHT_SMOKE;
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
                eventCode = BlockConstants.BLOCK_CODE_SPARK;
                break;
            case BlockConstants.BLOCK_CODE_LAVA:
                eventCode = BlockConstants.BLOCK_CODE_TAIL_SMOKE;
                break;
            case BlockConstants.BLOCK_CODE_SWAMP:
            case BlockConstants.BLOCK_CODE_WATER:
                eventCode = BlockConstants.BLOCK_CODE_SPRAY;
                break;
            case BlockConstants.BLOCK_CODE_BLACK:
            case BlockConstants.BLOCK_CODE_SAND:
            case BlockConstants.BLOCK_CODE_SNOW:
            default:
                break;
        }
        if (targetBlock.isPresent()) {
            switch (targetBlock.get().getBlockInfo().getType()) {
                case BlockConstants.BLOCK_TYPE_PLAYER:
                    eventCode = BlockConstants.BLOCK_CODE_BLEED;
                    break;
                case BlockConstants.BLOCK_TYPE_DROP:
                case BlockConstants.BLOCK_TYPE_TELEPORT:
                case BlockConstants.BLOCK_TYPE_TREE:
                    eventCode = BlockConstants.BLOCK_CODE_LIGHT_SMOKE;
                    break;
                case BlockConstants.BLOCK_TYPE_BED:
                case BlockConstants.BLOCK_TYPE_TOILET:
                case BlockConstants.BLOCK_TYPE_DRESSER:
                case BlockConstants.BLOCK_TYPE_STORAGE:
                case BlockConstants.BLOCK_TYPE_COOKER:
                case BlockConstants.BLOCK_TYPE_SINK:
                case BlockConstants.BLOCK_TYPE_CONTAINER:
                case BlockConstants.BLOCK_TYPE_SPEAKER:
                case BlockConstants.BLOCK_TYPE_BUILDING:
                case BlockConstants.BLOCK_TYPE_FARM:
                case BlockConstants.BLOCK_TYPE_ROCK:
                case BlockConstants.BLOCK_TYPE_WORKSHOP:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
                case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                case BlockConstants.BLOCK_TYPE_TRAP:
                    eventCode = BlockConstants.BLOCK_CODE_SPARK;
                    break;
                default:
                    break;
            }
        }
        if (null != eventCode) {
            addEvent(world, eventCode, fromCreature.getBlockInfo().getId(), bulletBlock.getWorldCoordinate());
        }
    }

    @Override
    public void updateEvents(GameWorld world) {
        world.getBlockMap().values()
                .forEach(block -> {
//                    worldService.expandByCoordinate(world, null, block.getWorldCoordinate(), 0);
                    updateEvent(world, block);
                });
    }

    private void updateEvent(GameWorld world, Block eventBlock) {
        Random random = new Random();
        eventBlock.getMovementInfo().setFrame(eventBlock.getMovementInfo().getFrame() + 1);
        updateEventLocation(world, eventBlock);
        if (eventBlock.getMovementInfo().getFrame() >= eventBlock.getMovementInfo().getPeriod()) {
            if (eventBlock.getMovementInfo().getFrameMax() == -1) {
                eventBlock.getMovementInfo().setFrame(eventBlock.getMovementInfo().getFrame() - eventBlock.getMovementInfo().getPeriod());
            } else {
                sceneManager.removeBlock(world, eventBlock, false);
                return;
            }
        }
        String fromId = world.getSourceMap().containsKey(eventBlock.getBlockInfo().getId())
                ? world.getSourceMap().get(eventBlock.getBlockInfo().getId())
                : eventBlock.getBlockInfo().getId();
        if (eventBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_TRAP) {
            switch (eventBlock.getBlockInfo().getCode()) {
                case BlockConstants.BLOCK_CODE_MINE:
                    if (world.getCreatureMap().values().stream()
                            .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                            .filter(player -> !StringUtils.equals(fromId, player.getBlockInfo().getId()))
                            .anyMatch(player -> {
                                BigDecimal distance = BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()), eventBlock.getWorldCoordinate(), player.getWorldCoordinate());
                                return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_MINE) < 0;
                            })) {
                        addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, fromId, eventBlock.getWorldCoordinate());
                        sceneManager.removeBlock(world, eventBlock, true);
                    }
                    break;
                case BlockConstants.BLOCK_CODE_FIRE:
                    // Burn grid
                    if (sceneManager.getGridBlockCode(world, eventBlock.getWorldCoordinate()) == BlockConstants.BLOCK_CODE_GRASS
                            || sceneManager.getGridBlockCode(world, eventBlock.getWorldCoordinate()) == BlockConstants.BLOCK_CODE_SNOW) {
                        sceneManager.setGridBlockCode(world, eventBlock.getWorldCoordinate(), BlockConstants.BLOCK_CODE_DIRT);
                    }
                    // Burn players only
                    world.getCreatureMap().values().stream()
                            .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                            .filter(player -> {
                                BigDecimal distance = BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()), eventBlock.getWorldCoordinate(), player.getWorldCoordinate());
                                return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                            })
                            .forEach(player -> {
                                affectBlock(world, eventBlock, player);
//                                addEvent(world, BlockConstants.BLOCK_CODE_BLEED, player.getBlockInfo().getId(), player.getWorldCoordinate());
                            });
                    break;
                case BlockConstants.BLOCK_CODE_WIRE_NETTING:
                    world.getCreatureMap().values().stream()
                            .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                            .filter(player -> {
                                BigDecimal distance = BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()), eventBlock.getWorldCoordinate(), player.getWorldCoordinate());
                                return null != distance && distance.compareTo(BlockConstants.WIRE_NETTING_RADIUS) < 0;
                            })
                            .forEach(player -> {
                                if (random.nextDouble()
                                        < Math.sqrt(Math.pow(player.getMovementInfo().getSpeed().getX().doubleValue(), 2)
                                        + Math.pow(player.getMovementInfo().getSpeed().getY().doubleValue(), 2))
                                        / player.getMovementInfo().getMaxSpeed().doubleValue()) {
                                    affectBlock(world, eventBlock, player);
//                                    addEvent(world, BlockConstants.BLOCK_CODE_BLEED, player.getBlockInfo().getId(), player.getWorldCoordinate());
                                }
                            });
                    break;
                default:
                    break;
            }
        }
    }

    private void updateEventLocation(GameWorld world, Block eventBlock) {
        String fromId = world.getSourceMap().containsKey(eventBlock.getBlockInfo().getId())
                ? world.getSourceMap().get(eventBlock.getBlockInfo().getId())
                : eventBlock.getBlockInfo().getId();
        if (eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_BLOCK
                || eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_HEAL
                || eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_SACRIFICE
                || eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_DECAY) {
            // Stick with playerInfo
            movementManager.settleCoordinate(world, eventBlock, world.getCreatureMap().get(fromId).getWorldCoordinate(), false);
        }
    }

    @Override
    public void affectBlock(GameWorld world, Block eventBlock, Block targetBlock) {
        if (eventBlock.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_EFFECT
                && eventBlock.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_TRAP) {
            logger.error(ErrorUtil.ERROR_1013);
            return;
        }
        int changedHp = SkillUtil.calculateChangedHp(eventBlock.getBlockInfo().getCode(),
                targetBlock.getBlockInfo().getType());
        if (targetBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                && world.getPlayerInfoMap().get(targetBlock.getBlockInfo().getId()).getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != 0) {
            changedHp /= 2;
        }
        if (targetBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                && world.getPlayerInfoMap().get(targetBlock.getBlockInfo().getId()).getPlayerType() != CreatureConstants.PLAYER_TYPE_HUMAN
                && !world.getNpcBrainMap().get(targetBlock.getBlockInfo().getId()).getExemption()[CreatureConstants.NPC_EXEMPTION_ALL]) {
            npcManager.prepare2Attack(world, targetBlock.getBlockInfo().getId(), world.getSourceMap().get(eventBlock.getBlockInfo().getId()));
        }
        changeHp(world, targetBlock, changedHp, false);
    }

    @Override
    @Transactional
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
                addEvent(world, BlockConstants.BLOCK_CODE_BLEED, block.getBlockInfo().getId(), bleedWc);
            }
            block.getBlockInfo().getHp().set(Math.max(0, Math.min(newHp, block.getBlockInfo().getHpMax().get())));
            if (block.getBlockInfo().getHp().get() <= 0 && playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0) {
                playerService.knockPlayer(block.getBlockInfo().getId());
            }
        } else {
            block.getBlockInfo().getHp().set(Math.max(0, Math.min(newHp, block.getBlockInfo().getHpMax().get())));
            if (block.getBlockInfo().getHp().get() <= 0) {
                addEvent(world, BlockConstants.BLOCK_CODE_TAIL_SMOKE, block.getBlockInfo().getId(), block.getWorldCoordinate());
                sceneManager.removeBlock(world, block, true);
            }
        }
    }
}
