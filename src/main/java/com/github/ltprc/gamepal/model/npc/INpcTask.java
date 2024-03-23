package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;

public interface INpcTask {

    int getNpcTaskType();

    void runNpcTask(String npcUserCode, WorldCoordinate wc);
}
