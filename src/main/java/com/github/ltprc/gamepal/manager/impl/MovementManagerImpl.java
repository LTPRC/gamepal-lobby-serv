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
            if (worldMovingBlock.getSpeed().getX().equals(BigDecimal.ZERO)
                    && worldMovingBlock.getSpeed().getY().equals(BigDecimal.ZERO)) {
                break;
            }
            Block block = rankingQueueList.get(i);
            if (block.getType() == GamePalConstants.BLOCK_TYPE_PLAYER && block.getId().equals(userCode)) {
                continue;
            }
            if (null == block.getStructure()) {
                block.setStructure(new Structure(GamePalConstants.STRUCTURE_UNDERSIDE_TYPE_SQUARE,
                        BigDecimal.valueOf(0.5D), BigDecimal.ZERO));
            }
            Structure structure = block.getStructure();
            Coordinate squareCoordinate = new Coordinate(block.getX(), block.getY().subtract(BigDecimal.valueOf(0.5)));
            if (block.getType() == GamePalConstants.BLOCK_TYPE_TELEPORT
                    && BlockUtil.detectCollisionSquare(worldMovingBlock.getCoordinate(),
                    new Coordinate(worldMovingBlock.getCoordinate().getX().add(worldMovingBlock.getSpeed().getX()),
                            worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY())),
                    squareCoordinate, worldMovingBlock.getStructure().getRadius(), structure.getRadius())) {
                    teleportWc = ((Teleport) block).getTo();
                    break;
            }
            if (!BlockUtil.checkBlockTypeSolid(block.getType())) {
                continue;
            }
            switch (structure.getUndersideType()) {
                case GamePalConstants.STRUCTURE_UNDERSIDE_TYPE_SQUARE:
                    // Blocked by square
                    if (BlockUtil.detectCollisionSquare(worldMovingBlock.getCoordinate(),
                            new Coordinate(worldMovingBlock.getCoordinate().getX()
                                    .add(worldMovingBlock.getSpeed().getX()),
                                    worldMovingBlock.getCoordinate().getY()),
                            squareCoordinate, worldMovingBlock.getStructure().getRadius(), structure.getRadius())) {
                        worldMovingBlock.getSpeed().setX(BigDecimal.ZERO);
                    }
                    if (BlockUtil.detectCollisionSquare(worldMovingBlock.getCoordinate(),
                            new Coordinate(worldMovingBlock.getCoordinate().getX(),
                                    worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY())),
                            squareCoordinate, worldMovingBlock.getStructure().getRadius(), structure.getRadius())) {
                        worldMovingBlock.getSpeed().setY(BigDecimal.ZERO);
                    }
                    break;
                case GamePalConstants.STRUCTURE_UNDERSIDE_TYPE_ROUND:
                    // Blocked by round
                    if (BlockUtil.detectCollision(worldMovingBlock.getCoordinate(),
                            new Coordinate(worldMovingBlock.getCoordinate().getX()
                                    .add(worldMovingBlock.getSpeed().getX()),
                                    worldMovingBlock.getCoordinate().getY()),
                            block, worldMovingBlock.getStructure().getRadius(), structure.getRadius())) {
                        worldMovingBlock.getSpeed().setX(BigDecimal.ZERO);
                    }
                    if (BlockUtil.detectCollision(worldMovingBlock.getCoordinate(),
                            new Coordinate(worldMovingBlock.getCoordinate().getX(),
                                    worldMovingBlock.getCoordinate().getY().add(worldMovingBlock.getSpeed().getY())),
                            block, worldMovingBlock.getStructure().getRadius(), structure.getRadius())) {
                        worldMovingBlock.getSpeed().setY(BigDecimal.ZERO);
                    }
                    break;
                default:
                    break;
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
