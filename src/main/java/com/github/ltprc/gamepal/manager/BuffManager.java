package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface BuffManager {

    void updateBuffTime(GameWorld world, String userCode);

    void checkBuff(GameWorld world, String userCode);

    void resetBuff(PlayerInfo playerInfo);

    void initializeBuff(PlayerInfo playerInfo);
}
