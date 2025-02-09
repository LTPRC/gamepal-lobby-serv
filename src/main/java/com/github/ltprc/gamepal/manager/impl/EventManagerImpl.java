package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.*;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;


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
        Block eventBlock = sceneManager.addOtherBlock(world, worldCoordinate, eventCode);
        if (StringUtils.isNotBlank(sourceId)) {
            world.getSourceMap().put(eventBlock.getBlockInfo().getId(), sourceId);
        }
        Block fromCreature = world.getCreatureMap().getOrDefault(sourceId, eventBlock);
        correctTarget(world, eventBlock, fromCreature);
        activateEvent(world, eventBlock, fromCreature);
    }

    private void correctTarget(GameWorld world, Block eventBlock, Block fromCreature) {
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        WorldCoordinate fromWorldCoordinate = fromCreature.getWorldCoordinate();
        if (worldCoordinate.getRegionNo() != fromWorldCoordinate.getRegionNo()) {
            return;
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        List<Block> preSelectedBlocks = sceneManager.collectAffectedBlocks(world, fromWorldCoordinate, eventBlock,
                fromCreature.getBlockInfo().getId());
        preSelectedBlocks.stream()
                .filter(blocker -> region.getRegionNo() == blocker.getWorldCoordinate().getRegionNo())
                .filter(blocker -> BlockUtil.checkMaterialCollision(
                        eventBlock.getBlockInfo().getStructure().getMaterial(),
                        blocker.getBlockInfo().getStructure().getMaterial()))
                .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                        BlockUtil.calculateAngle(region, fromWorldCoordinate, blocker.getWorldCoordinate()).doubleValue(),
                        BlockUtil.calculateAngle(region, fromWorldCoordinate, worldCoordinate).doubleValue()) < 135D)
                .filter(blocker -> BlockUtil.compareAnglesInDegrees(
                                BlockUtil.calculateAngle(region, fromWorldCoordinate, blocker.getWorldCoordinate()).doubleValue(),
                                fromCreature.getMovementInfo().getFaceDirection().doubleValue()) < 135D)
                .forEach(blocker ->
                        BlockUtil.detectLineCollision(region, fromWorldCoordinate, eventBlock, blocker, true)
                );
    }

    private void activateEvent(GameWorld world, Block eventBlock, Block fromCreature) {
        Random random = new Random();
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        WorldCoordinate fromWorldCoordinate = fromCreature.getWorldCoordinate();
        if (worldCoordinate.getRegionNo() != fromWorldCoordinate.getRegionNo()) {
            return;
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        List<Block> affectedBlockList = sceneManager.collectAffectedBlocks(world, fromWorldCoordinate, eventBlock,
                fromCreature.getBlockInfo().getId());
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
                                sceneManager.addOtherBlock(world, flameCoordinate, BlockConstants.BLOCK_CODE_FIRE);
//                                addEvent(world, BlockConstants.BLOCK_CODE_FIRE, fromCreature.getBlockInfo().getId(), flameCoordinate);
                            });
                }
                break;
            case BlockConstants.BLOCK_CODE_SHOOT_SPRAY:
                addEvent(world, BlockConstants.BLOCK_CODE_SPRAY, fromCreature.getBlockInfo().getId(), worldCoordinate);
                // Extinguish fire
                region.getScenes().values().stream()
                        .filter(scene -> SkillUtil.isSceneDetected(eventBlock, scene.getSceneCoordinate(), 1))
                        .forEach(scene -> scene.getBlocks().values().stream()
                                .filter(block -> block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLASMA)
                                .filter(block -> block.getBlockInfo().getCode() == BlockConstants.BLOCK_CODE_FIRE)
                                .filter(block -> {
                                    BigDecimal distance = BlockUtil.calculateDistance(
                                            world.getRegionMap().get(worldCoordinate.getRegionNo()),
                                            worldCoordinate, block.getWorldCoordinate());
                                    return null != distance && distance.compareTo(BlockConstants.FIRE_RADIUS) < 0;
                                })
                                .forEach(block -> sceneManager.removeBlock(world, block, true)));
                break;
            case BlockConstants.BLOCK_CODE_EXPLODE:
                sceneManager.setGridBlockCode(world, worldCoordinate, BlockConstants.BLOCK_CODE_LAVA);
                affectedBlockList.stream()
//                        .filter(affectedBlock -> affectedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER)
                        .forEach(block -> {
                            affectBlock(world, eventBlock, block);
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
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_SAD] != -1) {
                                playerInfo.getBuff()[BuffConstants.BUFF_CODE_SAD] = BuffConstants.BUFF_DEFAULT_FRAME_SAD;
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
                            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_HAPPY] != -1) {
                                playerInfo.getBuff()[BuffConstants.BUFF_CODE_HAPPY] = BuffConstants.BUFF_DEFAULT_FRAME_HAPPY;
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
            default:
                eventCode = BlockConstants.BLOCK_CODE_LIGHT_SMOKE;
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
                case BlockConstants.BLOCK_TYPE_WALL:
                case BlockConstants.BLOCK_TYPE_FLOOR:
                case BlockConstants.BLOCK_TYPE_CEILING:
                    eventCode = BlockConstants.BLOCK_CODE_LIGHT_SMOKE;
                    break;
                default:
                    eventCode = BlockConstants.BLOCK_CODE_SPARK;
                    break;
            }
        }
        addEvent(world, eventCode, fromCreature.getBlockInfo().getId(), bulletBlock.getWorldCoordinate());
    }

    @Override
    @Transactional
    public void updateEvent(GameWorld world, Block eventBlock) {
        if (eventBlock.getMovementInfo().getPeriod() == BlockConstants.PERIOD_STATIC_DEFAULT) {
            return;
        }
        eventBlock.getMovementInfo().setFrame(eventBlock.getMovementInfo().getFrame() + 1);
        updateEventLocation(world, eventBlock);
        if (eventBlock.getMovementInfo().getFrameMax() != BlockConstants.FRAME_MAX_INFINITE_DEFAULT
                && eventBlock.getMovementInfo().getFrame() >= eventBlock.getMovementInfo().getFrameMax()) {
            sceneManager.removeBlock(world, eventBlock, false);
            return;
        }
        if (eventBlock.getMovementInfo().getFrame() >= eventBlock.getMovementInfo().getPeriod()) {
            eventBlock.getMovementInfo().setFrame(eventBlock.getMovementInfo().getFrame()
                    - eventBlock.getMovementInfo().getPeriod());
            eventBlock.getMovementInfo().setFrameMax(Math.max(0,
                    eventBlock.getMovementInfo().getFrameMax() - eventBlock.getMovementInfo().getPeriod()));
        }
        if (eventBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLASMA) {
//            triggerTrap(world, eventBlock);
            if (eventBlock.getBlockInfo().getCode() == BlockConstants.BLOCK_CODE_FIRE) {
                // Burn grid
                if (sceneManager.getGridBlockCode(world, eventBlock.getWorldCoordinate()) == BlockConstants.BLOCK_CODE_GRASS
                        || sceneManager.getGridBlockCode(world, eventBlock.getWorldCoordinate()) == BlockConstants.BLOCK_CODE_SNOW) {
                    sceneManager.setGridBlockCode(world, eventBlock.getWorldCoordinate(), BlockConstants.BLOCK_CODE_DIRT);
                }
                // Burn collected blocks 25/02/03
                Queue<Block> rankingQueue = sceneManager.collectSurroundingBlocks(world, eventBlock, 1);
                rankingQueue.stream()
                        .filter(targetBlock -> targetBlock.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_PLAYER
                                || playerService.validateActiveness(world, targetBlock.getBlockInfo().getId()))
                        .filter(targetBlock -> {
                            BigDecimal distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                    eventBlock.getWorldCoordinate(), targetBlock.getWorldCoordinate());
                            return null != distance && distance.compareTo(BlockConstants.FIRE_RADIUS) < 0;
                        })
                        .forEach(targetBlock -> {
                            affectBlock(world, eventBlock, targetBlock);
                        });
            }
        }
    }

    private void updateEventLocation(GameWorld world, Block eventBlock) {
        String fromId = world.getSourceMap().containsKey(eventBlock.getBlockInfo().getId())
                ? world.getSourceMap().get(eventBlock.getBlockInfo().getId())
                : eventBlock.getBlockInfo().getId();
        if (!world.getCreatureMap().containsKey(fromId)) {
            return;
        }
        if (eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_BLOCK
                || eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_HEAL
                || eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_SACRIFICE
                || eventBlock.getBlockInfo().getCode() ==  BlockConstants.BLOCK_CODE_DECAY) {
            movementManager.settleCoordinate(world, eventBlock, world.getCreatureMap().get(fromId).getWorldCoordinate(),
                    false);
        }
    }

    @Override
    @Transactional
    public void affectBlock(GameWorld world, Block eventBlock, Block targetBlock) {
        int changedHp = SkillUtil.calculateChangedHp(eventBlock.getBlockInfo().getCode(),
                targetBlock.getBlockInfo().getType());
        if (targetBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                && world.getPlayerInfoMap().get(targetBlock.getBlockInfo().getId()).getBuff()[BuffConstants.BUFF_CODE_BLOCKED] != 0) {
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
        int oldHp = block.getBlockInfo().getHp().get();
        int newHp = isAbsolute ? value : oldHp + value;
        if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER) {
            Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
            PlayerInfo playerInfo = playerInfoMap.get(block.getBlockInfo().getId());
            if (playerInfo.getBuff()[BuffConstants.BUFF_CODE_INVINCIBLE] != 0) {
                newHp = Math.max(oldHp, newHp);
            }
            if (newHp < oldHp) {
                addEvent(world, BlockConstants.BLOCK_CODE_BLEED, block.getBlockInfo().getId(), block.getWorldCoordinate());
            }
            block.getBlockInfo().getHp().set(Math.max(0, Math.min(newHp, block.getBlockInfo().getHpMax().get())));
            if (block.getBlockInfo().getHp().get() <= 0 && playerInfo.getBuff()[BuffConstants.BUFF_CODE_DEAD] == 0) {
                playerService.knockPlayer(block.getBlockInfo().getId());
            }
        } else {
            if (newHp < oldHp) {
                addEvent(world, BlockConstants.BLOCK_CODE_DISINTEGRATE, block.getBlockInfo().getId(), block.getWorldCoordinate());
            }
            block.getBlockInfo().getHp().set(Math.max(0, Math.min(newHp, block.getBlockInfo().getHpMax().get())));
            if (block.getBlockInfo().getHp().get() <= 0) {
                addEvent(world, BlockConstants.BLOCK_CODE_TAIL_SMOKE, block.getBlockInfo().getId(), block.getWorldCoordinate());
                sceneManager.removeBlock(world, block, true);
            }
        }
    }
}
