package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;

public interface MovementManager {

    void settleSpeedAndCoordinate(GameWorld world, Block block, int sceneScanDepth);

    void settleCoordinate(GameWorld world, Block block, WorldCoordinate newWorldCoordinate, boolean isTeleport);

    void syncFloorCode(GameWorld world, Block block);
}
