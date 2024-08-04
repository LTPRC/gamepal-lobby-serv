package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface BuffManager {

    void updateBuffTime(GameWorld world, String userCode);

    void activateBuff(GameWorld world, String userCode);

    void changeBuff(GameWorld world, String userCode);
}
