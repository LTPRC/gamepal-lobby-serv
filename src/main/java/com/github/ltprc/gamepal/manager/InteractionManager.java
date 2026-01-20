package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface InteractionManager {
    void focusOnBlocks(GameWorld world, String userCode);
    void focusOnBlock(GameWorld world, String userCode, Block block);
    void interactBlocks(GameWorld world, String userCode, int interactionCode);
}
