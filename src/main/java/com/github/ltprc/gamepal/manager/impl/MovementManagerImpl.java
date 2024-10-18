package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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
    public void settleSpeedAndCoordinate(GameWorld world, Block worldMovingBlock, int sceneScanDepth) {
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        Queue<Block> rankingQueue = sceneManager.collectBlocksByUserCode(world, worldMovingBlock, 1);
        WorldCoordinate teleportWc = null;
        List<Block> rankingQueueList = new ArrayList<>(rankingQueue);
        for (int i = 0; i < rankingQueueList.size(); i ++) {
            if (worldMovingBlock.getMovementInfo().getSpeed().getX().equals(BigDecimal.ZERO)
                    && worldMovingBlock.getMovementInfo().getSpeed().getY().equals(BigDecimal.ZERO)) {
                break;
            }
            Block block = rankingQueueList.get(i);
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
//            if (BlockConstants.STRUCTURE_MATERIAL_HOLLOW == block.getBlockInfo().getStructure().getMaterial()) {
//                continue;
//            }
//            if (BlockUtil.checkMaterialCollision(worldMovingBlock.getBlockInfo().getStructure().getMaterial(),
//                    block.getBlockInfo().getStructure().getMaterial())) {
//                continue;
//            }
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
        } else {
            worldMovingBlock.getMovementInfo().setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
        }
        worldService.expandByCoordinate(world, worldMovingBlock.getWorldCoordinate(), teleportWc, sceneScanDepth);
        settleCoordinate(world, worldMovingBlock, teleportWc);
    }

    @Override
    public void settleCoordinate(GameWorld world, Block worldMovingBlock,
                                 WorldCoordinate newWorldCoordinate) {
        boolean isRegionChanged = worldMovingBlock.getWorldCoordinate().getRegionNo() != newWorldCoordinate.getRegionNo();
        boolean isSceneChanged = isRegionChanged
                || !worldMovingBlock.getWorldCoordinate().getSceneCoordinate().getX()
                .equals(newWorldCoordinate.getSceneCoordinate().getX())
                || !worldMovingBlock.getWorldCoordinate().getSceneCoordinate().getY()
                .equals(newWorldCoordinate.getSceneCoordinate().getY());

        BlockUtil.copyWorldCoordinate(newWorldCoordinate, worldMovingBlock.getWorldCoordinate());
        Region region = world.getRegionMap().get(worldMovingBlock.getWorldCoordinate().getRegionNo());
        BlockUtil.fixWorldCoordinate(region, worldMovingBlock.getWorldCoordinate());
        if (!world.getCreatureMap().containsKey(worldMovingBlock.getBlockInfo().getId())) {
            return;
        }
        PlayerInfo playerInfo = world.getCreatureMap().get(worldMovingBlock.getBlockInfo().getId()).getPlayerInfo();
        if (playerInfo.getPlayerType() == CreatureConstants.PLAYER_TYPE_HUMAN) {
            // Check location change
            if (isSceneChanged) {
                Scene scene = region.getScenes().get(worldMovingBlock.getWorldCoordinate().getSceneCoordinate());
                playerService.generateNotificationMessage(worldMovingBlock.getBlockInfo().getId(),
                        "来到【" + region.getName() + "-" + scene.getName() + "】");
            }
            // Check drop
            world.getBlockMap().values().stream()
                    .filter(worldMovingBlock1 -> worldMovingBlock1.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_DROP)
                    .filter(worldMovingBlock1 -> worldMovingBlock1.getWorldCoordinate().getRegionNo() == region.getRegionNo())
                    .filter(worldMovingBlock1 ->
                            BlockUtil.calculateDistance(region, worldMovingBlock.getWorldCoordinate(),
                                            worldMovingBlock1.getWorldCoordinate())
                                    .compareTo(BlockConstants.MIN_DROP_INTERACTION_DISTANCE) < 0)
                    .forEach(drop -> playerService.useDrop(worldMovingBlock.getBlockInfo().getId(), drop.getBlockInfo().getId()));
            // TODO Check interaction
        }
        syncFloorCode(world, worldMovingBlock);
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
        IntegerCoordinate gridCoordinate = new IntegerCoordinate(
                worldMovingBlock.getWorldCoordinate().getCoordinate().getX().add(BigDecimal.valueOf(0.5D)).intValue(),
                worldMovingBlock.getWorldCoordinate().getCoordinate().getY().add(BigDecimal.valueOf(0.5D)).intValue());
        int floorCode = scene.getGird()[gridCoordinate.getX()][gridCoordinate.getY()];
        worldMovingBlock.getMovementInfo().setFloorCode(floorCode);
    }
}
