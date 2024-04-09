package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
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
