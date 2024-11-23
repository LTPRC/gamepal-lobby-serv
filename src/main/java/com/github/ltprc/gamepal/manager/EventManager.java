package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface EventManager {

//    BlockInfo createBlockInfoByEventCode(final int eventCode);
//    MovementInfo createMovementInfoByEventCode(final int eventCode);
    void addEvent(GameWorld world, int eventCode, String sourceId, WorldCoordinate worldCoordinate);
    void updateEvents(GameWorld world);
    void affectBlock(GameWorld world, Block eventBlock, Block targetBlock);
    void changeHp(GameWorld world, Block block, int value, boolean isAbsolute);
}
