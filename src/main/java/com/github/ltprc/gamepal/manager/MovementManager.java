package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;

public interface MovementManager {

    void settleSpeedAndCoordinate(GameWorld world, WorldMovingBlock worldMovingBlock, int sceneScanDepth);

    void settleCoordinate(GameWorld world, WorldMovingBlock worldMovingBlock, WorldCoordinate newWorldCoordinate);

    void syncFloorCode(GameWorld world, WorldMovingBlock worldMovingBlock);
}
