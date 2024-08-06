package com.github.ltprc.gamepal.manager;


import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;

import java.math.BigDecimal;

public interface NpcManager {

    PlayerInfo createCreature(GameWorld world, final int playerType, final int creatureType, String userCode);

    PlayerInfo putCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate);

    JSONObject changeNpcBehavior(JSONObject request);

    PlayerInfo putSpecificCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate,
                                   final BigDecimal distance, final int playerType, final int creatureType,
                                   final int behavior, final int stance, final boolean[] exemption);

    PlayerInfo putSpecificCreatureByRole(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate,
                             final int role);

    void updateNpcBrains(GameWorld world);

    boolean prepare2Attack(GameWorld world, final String userCode, final String npcUserCode);

    void resetNpcBrainQueues(String npcUserCode);
}
