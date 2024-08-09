package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    public void settleSpeedAndCoordinate(GameWorld world, WorldMovingBlock worldMovingBlock) {
        String userCode = worldMovingBlock.getId();
        Region region = world.getRegionMap().get(worldMovingBlock.getRegionNo());
        Queue<Block> rankingQueue = sceneManager.collectBlocksByUserCode(userCode, 1);
        WorldCoordinate teleportWc = null;
        List<Block> rankingQueueList = new ArrayList<>(rankingQueue);
        for (int i = 0; i < rankingQueueList.size(); i ++) {
            if (worldMovingBlock.getSpeed().getX().equals(BigDecimal.ZERO)
                    && worldMovingBlock.getSpeed().getY().equals(BigDecimal.ZERO)) {
                break;
            }
            Block block = rankingQueueList.get(i);
            if (block.getType() == GamePalConstants.BLOCK_TYPE_PLAYER && block.getId().equals(userCode)) {
                continue;
            }
            Block movingBlock = BlockUtil.convertWorldBlock2Block(region, worldMovingBlock, false);
            Block newMovingBlock = new Block(movingBlock);
            newMovingBlock.setX(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()));
            newMovingBlock.setY(worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY()));
            if (block.getType() == GamePalConstants.BLOCK_TYPE_TELEPORT
                    && BlockUtil.detectCollision(newMovingBlock, block)) {
                    teleportWc = ((Teleport) block).getTo();
                    break;
            }
            if (GamePalConstants.STRUCTURE_MATERIAL_HOLLOW == block.getStructure().getMaterial()) {
                continue;
            }
            newMovingBlock = new Block(movingBlock);
            newMovingBlock.setX(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()));
            if (!BlockUtil.detectCollision(movingBlock, block) && BlockUtil.detectCollision(newMovingBlock, block)) {
                worldMovingBlock.getSpeed().setX(BigDecimal.ZERO);
            }
            newMovingBlock = new Block(movingBlock);
            newMovingBlock.setY(worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY()));
            if (!BlockUtil.detectCollision(movingBlock, block) && BlockUtil.detectCollision(newMovingBlock, block)) {
                worldMovingBlock.getSpeed().setY(BigDecimal.ZERO);
            }
        }
        // Settle worldMovingBlock position
        if (null == teleportWc) {
            settleCoordinate(world, worldMovingBlock, new WorldCoordinate(
                    worldMovingBlock.getRegionNo(),
                    worldMovingBlock.getSceneCoordinate(),
                    new Coordinate(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()),
                            worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY()))), false);
            BlockUtil.fixWorldCoordinate(region, worldMovingBlock);
        } else {
            worldService.expandScene(world, teleportWc);
            worldMovingBlock.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            settleCoordinate(world, worldMovingBlock, teleportWc, false);
        }
    }

    @Override
    public void settleCoordinate(GameWorld world, WorldMovingBlock worldMovingBlock,
                                 WorldCoordinate newWorldCoordinate, boolean returnToUser) {
//        boolean isRegionChanged = worldMovingBlock.getRegionNo() != newWorldCoordinate.getRegionNo();
//        boolean isSceneChanged = isRegionChanged
//                || !worldMovingBlock.getSceneCoordinate().getX().equals(newWorldCoordinate.getSceneCoordinate().getX())
//                || !worldMovingBlock.getSceneCoordinate().getY().equals(newWorldCoordinate.getSceneCoordinate().getY());
        BlockUtil.copyWorldCoordinate(newWorldCoordinate, worldMovingBlock);
//        if (isSceneChanged) {
//            Region region = world.getRegionMap().get(worldMovingBlock.getRegionNo());
//            Scene scene = region.getScenes().get(worldMovingBlock.getSceneCoordinate());
//            playerService.generateNotificationMessage(worldMovingBlock.getId(),
//                    "来到【" + region.getName() + "-" + scene.getName() + "】");
//        }
    }
}
