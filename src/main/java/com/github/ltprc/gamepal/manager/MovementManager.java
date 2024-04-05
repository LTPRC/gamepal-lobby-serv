package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;

public interface MovementManager {

    void settleSpeed(String userCode, WorldMovingBlock worldMovingBlock);
}
