package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface InteractionManager {
    void searchInteraction(GameWorld world, String userCode);
    void interactBlocks(GameWorld world, String userCode, int interactionCode);
}
