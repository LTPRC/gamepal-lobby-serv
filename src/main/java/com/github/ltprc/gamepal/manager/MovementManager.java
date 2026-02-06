package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.coordinate.PlanarCoordinate;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;

import java.math.BigDecimal;

public interface MovementManager {
    void settleCreatureAcceleration(GameWorld world, Block block, PlanarCoordinate accelerationCoordinate, int movementMode);
    void settleGravityAcceleration(GameWorld world, Block block);
    void settleAcceleration(GameWorld world, Block block, Coordinate acceleration, BigDecimal maxPlanarSpeed,
                            BigDecimal maxVerticalSpeed);
    void settleSpeed(GameWorld world, Block worldMovingBlock);
    void settleCoordinate(GameWorld world, Block block, final WorldCoordinate newWorldCoordinate, boolean isTeleport);
    void syncFloorCode(GameWorld world, Block block);
    void updateCreatureMaxSpeed(GameWorld world, String userCode);
    boolean detectCollision(GameWorld world, Block block1, Block block2, boolean relocate);
    boolean detectLinearCollision(GameWorld world, WorldCoordinate from, Block block1, Block block2, boolean relocate);
    boolean detectSectorInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                  BigDecimal sectorAngle);
    boolean detectCylinderInfluence(GameWorld world, WorldCoordinate from, Block block1, Block block2,
                                    BigDecimal planarDistance, BigDecimal verticalDistance);
    void applyFriction(Block player);
}
