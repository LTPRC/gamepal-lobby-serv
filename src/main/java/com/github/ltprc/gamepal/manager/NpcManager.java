package com.github.ltprc.gamepal.manager;


import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;

import java.math.BigDecimal;

public interface NpcManager {

    Block createCreature(GameWorld world, final int playerType, final int creatureType, String userCode);

    Block putCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate);

    JSONObject changeNpcBehavior(JSONObject request);

    Block putSpecificCreature(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate,
                                   final BigDecimal distance, final int playerType, final int creatureType,
                                   final int behavior, final int stance, final boolean[] exemption);

    Block putSpecificCreatureByRole(GameWorld world, final String userCode, final WorldCoordinate worldCoordinate,
                             final int role);

    void updateNpcBrains(GameWorld world);

    boolean prepare2Attack(GameWorld world, final String userCode, final String npcUserCode);

    void resetNpcBrainQueues(String npcUserCode);
}
