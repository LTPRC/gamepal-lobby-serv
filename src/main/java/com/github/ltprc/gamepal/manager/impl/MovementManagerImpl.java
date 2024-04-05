package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Component
public class MovementManagerImpl implements MovementManager {

    @Autowired
    private UserService userService;

    @Autowired
    private SceneManager sceneManager;

    @Override
    public void settleSpeed(String userCode, WorldMovingBlock worldMovingBlock) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Region region = world.getRegionMap().get(worldMovingBlock.getRegionNo());
        Queue<Block> rankingQueue = sceneManager.collectBlocksByUserCode(userCode, 1);
        WorldCoordinate teleportWc = null;
        List<Block> rankingQueueList = new ArrayList<>(rankingQueue);
        for (int i = 0; i < rankingQueueList.size(); i ++) {
            Block block = rankingQueueList.get(i);
            if (block.getType() == GamePalConstants.BLOCK_TYPE_TELEPORT) {
                if (BlockUtil.detectCollisionSquare(worldMovingBlock.getCoordinate(),
                        new Coordinate(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()),
                                worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY())),
                        block, GamePalConstants.PLAYER_RADIUS, BigDecimal.ONE)) {
                    teleportWc = ((Teleport) block).getTo();
                    break;
                }
            } else if (block.getType() == GamePalConstants.BLOCK_TYPE_PLAYER
                    || block.getType() == GamePalConstants.BLOCK_TYPE_TREE) {
                if (BlockUtil.detectCollision(worldMovingBlock.getCoordinate(),
                        new Coordinate(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()),
                                worldMovingBlock.getCoordinate().getY()),
                        block, GamePalConstants.PLAYER_RADIUS.multiply(BigDecimal.valueOf(2)))) {
                    worldMovingBlock.getSpeed().setX(BigDecimal.ZERO);
                }
                if (BlockUtil.detectCollision(worldMovingBlock.getCoordinate(),
                        new Coordinate(worldMovingBlock.getCoordinate().getX(),
                                worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY())),
                        block, GamePalConstants.PLAYER_RADIUS.multiply(BigDecimal.valueOf(2)))) {
                    worldMovingBlock.getSpeed().setY(BigDecimal.ZERO);
                }
            } else if (BlockUtil.checkBlockSolid(block.getType())) {
                if (BlockUtil.detectCollisionSquare(worldMovingBlock.getCoordinate(),
                        new Coordinate(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()),
                                worldMovingBlock.getCoordinate().getY()),
                        block, GamePalConstants.PLAYER_RADIUS, BigDecimal.ONE)) {
                    worldMovingBlock.getSpeed().setX(BigDecimal.ZERO);
                }
                if (BlockUtil.detectCollisionSquare(worldMovingBlock.getCoordinate(),
                        new Coordinate(worldMovingBlock.getCoordinate().getX(),
                                worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY())),
                        block, GamePalConstants.PLAYER_RADIUS, BigDecimal.ONE)) {
                    worldMovingBlock.getSpeed().setY(BigDecimal.ZERO);
                }
            }
        }
        // Settle worldMovingBlock position
        if (null == teleportWc) {
            worldMovingBlock.getCoordinate().setX(worldMovingBlock.getCoordinate().getX()
                    .add(worldMovingBlock.getSpeed().getX()));
            worldMovingBlock.getCoordinate().setY(worldMovingBlock.getCoordinate().getY()
                    .add(worldMovingBlock.getSpeed().getY()));
        } else {
            BlockUtil.copyWorldCoordinate(teleportWc, worldMovingBlock);
            worldMovingBlock.setSpeed(new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO));
            region = world.getRegionMap().get(teleportWc.getRegionNo());
        }
        BlockUtil.fixWorldCoordinate(region, worldMovingBlock);
    }
}
