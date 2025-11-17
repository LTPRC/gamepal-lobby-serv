package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;

public interface MovementManager {
    void speedUpBlock(GameWorld world, Block block, Coordinate deltaSpeed);
    void settleAcceleration(GameWorld world, Block block, Coordinate accelerationCoordinate, int movementMode);
    void settleSpeedAndCoordinate(GameWorld world, Block block, int sceneScanDepth);
    void settleCoordinate(GameWorld world, Block block, final WorldCoordinate newWorldCoordinate, boolean isTeleport);
    void syncFloorCode(GameWorld world, Block block);
    void updateCreatureMaxSpeed(GameWorld world, String userCode);
    boolean detectCollision(GameWorld world, Block block1, Block block2);
    boolean detectLineCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2, boolean correctBlock1);
}
