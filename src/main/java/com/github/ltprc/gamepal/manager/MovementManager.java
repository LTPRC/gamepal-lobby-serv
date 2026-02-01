package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.coordinate.PlanarCoordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;

import java.math.BigDecimal;

public interface MovementManager {
    void speedUpBlock(GameWorld world, Block block, Coordinate deltaSpeed);
    void settlePlanarAcceleration(GameWorld world, Block block, PlanarCoordinate accelerationCoordinate, int movementMode);
    void settleVerticalAcceleration(GameWorld world, Block block);
    void settlePlanarSpeed(GameWorld world, Block block, int sceneScanDepth);
    void settleVerticalSpeed(GameWorld world, Block block);
    void settleCoordinate(GameWorld world, Block block, final WorldCoordinate newWorldCoordinate, boolean isTeleport);
    void syncFloorCode(GameWorld world, Block block);
    void updateCreatureMaxSpeed(GameWorld world, String userCode);
    boolean detectCollision(GameWorld world, Block block1, Block block2, boolean relocate);
    boolean detectLinearCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2, boolean relocate);
    boolean detectSectorCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                  BigDecimal sectorAngle);
    boolean detectCylinderInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                    BigDecimal planarDistance, BigDecimal verticalDistance);
}
