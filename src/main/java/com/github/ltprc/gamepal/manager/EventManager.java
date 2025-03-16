package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface EventManager {
    void addEvent(GameWorld world, int eventCode, String sourceId, WorldCoordinate worldCoordinate);
    void updateEvent(GameWorld world, Block eventBlock);
    void affectBlock(GameWorld world, Block eventBlock, Block targetBlock);
    void changeHp(GameWorld world, Block block, int value, boolean isAbsolute);
}
