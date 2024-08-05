package com.github.ltprc.gamepal.manager;


import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;

public interface NpcManager {

    PlayerInfo createCreature(GameWorld world, final int playerType, String userCode);

    void putCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate);

    JSONObject changeNpcBehavior(JSONObject request);

    void updateNpcBrains(GameWorld world);

    void resetNpcBrainQueues(String npcUserCode);
}
