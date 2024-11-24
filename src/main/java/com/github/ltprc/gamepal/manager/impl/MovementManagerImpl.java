package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class MovementManagerImpl implements MovementManager {

    private static final Log logger = LogFactory.getLog(MovementManagerImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private WorldService worldService;

    @Autowired
    private PlayerService playerService;

    @Override
    public void speedUpBlock(GameWorld world, Block block, Coordinate deltaSpeed) {
        MovementInfo movementInfo = block.getMovementInfo();
        movementInfo.getSpeed().setX(movementInfo.getSpeed().getX().add(deltaSpeed.getX()));
        movementInfo.getSpeed().setY(movementInfo.getSpeed().getY().add(deltaSpeed.getY()));
        movementInfo.setFaceDirection(BlockUtil.calculateAngle(new Coordinate(), movementInfo.getSpeed()));
        settleSpeedAndCoordinate(world, block, 0);
    }

    @Override
    public void settleSpeedAndCoordinate(GameWorld world, Block worldMovingBlock, int sceneScanDepth) {
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        Queue<Block> rankingQueue = sceneManager.collectBlocksByUserCode(world, worldMovingBlock, 1);
        WorldCoordinate teleportWc = null;
        List<Block> rankingQueueList = new ArrayList<>(rankingQueue);
        for (Block block : rankingQueueList) {
            if (worldMovingBlock.getMovementInfo().getSpeed().getX().equals(BigDecimal.ZERO)
                    && worldMovingBlock.getMovementInfo().getSpeed().getY().equals(BigDecimal.ZERO)) {
                break;
            }
            if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                    && block.getBlockInfo().getId().equals(worldMovingBlock.getBlockInfo().getId())) {
                continue;
            }
            Block newMovingBlock = new Block(worldMovingBlock);
            newMovingBlock.getWorldCoordinate().getCoordinate().setX(
                    worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                            .add(worldMovingBlock.getMovementInfo().getSpeed().getX()));
            newMovingBlock.getWorldCoordinate().getCoordinate().setY(
                    worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                            .add(worldMovingBlock.getMovementInfo().getSpeed().getY()));
            if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_TELEPORT
                    && BlockUtil.detectCollision(region, newMovingBlock, block)) {
                teleportWc = world.getTeleportMap().get(block.getBlockInfo().getId());
                break;
            }
            newMovingBlock = new Block(worldMovingBlock);
            newMovingBlock.getWorldCoordinate().getCoordinate().setX(
                    worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                            .add(worldMovingBlock.getMovementInfo().getSpeed().getX()));
            if (!BlockUtil.detectCollision(region, worldMovingBlock, block)
                    && BlockUtil.detectCollision(region, newMovingBlock, block)
                    && BlockUtil.checkMaterialCollision(newMovingBlock.getBlockInfo().getStructure().getMaterial(),
                    block.getBlockInfo().getStructure().getMaterial())) {
                worldMovingBlock.getMovementInfo().getSpeed().setX(BigDecimal.ZERO);
            }
            newMovingBlock = new Block(worldMovingBlock);
            newMovingBlock.getWorldCoordinate().getCoordinate().setY(
                    worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                            .add(worldMovingBlock.getMovementInfo().getSpeed().getY()));
            if (!BlockUtil.detectCollision(region, worldMovingBlock, block)
                    && BlockUtil.detectCollision(region, newMovingBlock, block)
                    && BlockUtil.checkMaterialCollision(newMovingBlock.getBlockInfo().getStructure().getMaterial(),
                    block.getBlockInfo().getStructure().getMaterial())) {
                worldMovingBlock.getMovementInfo().getSpeed().setY(BigDecimal.ZERO);
            }
        }
        // Settle worldMovingBlock position
        if (null == teleportWc) {
            teleportWc = new WorldCoordinate(
                    worldMovingBlock.getWorldCoordinate().getRegionNo(),
                    worldMovingBlock.getWorldCoordinate().getSceneCoordinate(),
                    new Coordinate(worldMovingBlock.getWorldCoordinate().getCoordinate().getX()
                            .add(worldMovingBlock.getMovementInfo().getSpeed().getX()),
                            worldMovingBlock.getWorldCoordinate().getCoordinate().getY()
                                    .add(worldMovingBlock.getMovementInfo().getSpeed().getY())));
            BlockUtil.fixWorldCoordinate(region, teleportWc);
            settleCoordinate(world, worldMovingBlock, teleportWc, false);
        } else {
            worldMovingBlock.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            worldService.expandByCoordinate(world, worldMovingBlock.getWorldCoordinate(), teleportWc, sceneScanDepth);
            settleCoordinate(world, worldMovingBlock, teleportWc, true);
        }
    }

    @Override
    public void settleCoordinate(GameWorld world, Block worldMovingBlock, final WorldCoordinate newWorldCoordinate,
                                 boolean isTeleport) {
        final WorldCoordinate oldWorldCoordinate = new WorldCoordinate(worldMovingBlock.getWorldCoordinate());
        worldService.expandByCoordinate(world, oldWorldCoordinate, newWorldCoordinate, 1);
        boolean isRegionChanged = oldWorldCoordinate.getRegionNo() != newWorldCoordinate.getRegionNo();
        boolean isSceneChanged = isRegionChanged
                || !oldWorldCoordinate.getSceneCoordinate().getX()
                .equals(newWorldCoordinate.getSceneCoordinate().getX())
                || !oldWorldCoordinate.getSceneCoordinate().getY()
                .equals(newWorldCoordinate.getSceneCoordinate().getY());

        BlockUtil.copyWorldCoordinate(newWorldCoordinate, worldMovingBlock.getWorldCoordinate());
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        BlockUtil.fixWorldCoordinate(region, worldMovingBlock.getWorldCoordinate());
        syncFloorCode(world, worldMovingBlock);
        if (worldMovingBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER) {
            if (world.getPlayerInfoMap().get(worldMovingBlock.getBlockInfo().getId()).getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN) {
                if (isTeleport) {
                    world.getFlagMap().get(worldMovingBlock.getBlockInfo().getId())[FlagConstants.FLAG_UPDATE_MOVEMENT] = true;
                }
                // Check location change
                if (isSceneChanged) {
                    Scene scene = region.getScenes().get(worldMovingBlock.getWorldCoordinate().getSceneCoordinate());
                    playerService.generateNotificationMessage(worldMovingBlock.getBlockInfo().getId(),
                            "来到【" + region.getName() + "-" + scene.getName() + "】");
                }
            }
            region.getScenes().values().stream()
                    .filter(scene -> SkillUtil.isSceneDetected(worldMovingBlock, scene.getSceneCoordinate(), 1))
                    .forEach(scene -> scene.getBlocks().values().forEach(block -> {
                BigDecimal distance = BlockUtil.calculateDistance(region, worldMovingBlock.getWorldCoordinate(),
                        block.getWorldCoordinate());
                if (block.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_DROP
                        && null != distance && distance.compareTo(BlockConstants.MIN_DROP_INTERACTION_DISTANCE) < 0) {
                    playerService.useDrop(worldMovingBlock.getBlockInfo().getId(), block.getBlockInfo().getId());
                }
            }));
        } else if (isSceneChanged) {
            region.getScenes().get(newWorldCoordinate.getSceneCoordinate()).getBlocks()
                    .put(worldMovingBlock.getBlockInfo().getId(), worldMovingBlock);
            region.getScenes().get(oldWorldCoordinate.getSceneCoordinate()).getBlocks()
                    .remove(worldMovingBlock.getBlockInfo().getId());
        }
    }

    @Override
    public void syncFloorCode(GameWorld world, Block worldMovingBlock) {
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        if (null == region) {
            logger.error(ErrorUtil.ERROR_1027);
            worldService.expandRegion(world, worldMovingBlock.getWorldCoordinate().getRegionNo());
            return;
        }
        Scene scene = region.getScenes().get(worldMovingBlock.getWorldCoordinate().getSceneCoordinate());
        if (null == scene) {
            logger.error(ErrorUtil.ERROR_1041);
            worldService.expandScene(world, worldMovingBlock.getWorldCoordinate(), 1);
            return;
        }
        if (null != scene.getGird() && null != scene.getGird()[0]) {
            WorldCoordinate worldCoordinate = new WorldCoordinate(worldMovingBlock.getWorldCoordinate());
            BlockUtil.fixWorldCoordinateReal(region, worldCoordinate);
            IntegerCoordinate gridCoordinate = BlockUtil.convertCoordinate2BasicIntegerCoordinate(worldCoordinate);
            int code1 = scene.getGird()[gridCoordinate.getX()][gridCoordinate.getY()];
            int code2 = scene.getGird()[gridCoordinate.getX() + 1][gridCoordinate.getY()];
            int code3 = scene.getGird()[gridCoordinate.getX()][gridCoordinate.getY() + 1];
            int code4 = scene.getGird()[gridCoordinate.getX() + 1][gridCoordinate.getY() + 1];
            if (code1 == BlockConstants.BLOCK_CODE_WATER && code2 == BlockConstants.BLOCK_CODE_WATER
                    && code3 == BlockConstants.BLOCK_CODE_WATER && code4 == BlockConstants.BLOCK_CODE_WATER) {
                worldMovingBlock.getMovementInfo().setFloorCode(BlockConstants.BLOCK_CODE_WATER);
                return;
            }
            int code = sceneManager.getGridBlockCode(world, worldMovingBlock.getWorldCoordinate());
            if (code != BlockConstants.BLOCK_CODE_WATER) {
                worldMovingBlock.getMovementInfo().setFloorCode(code);
            }
        }
    }
}
