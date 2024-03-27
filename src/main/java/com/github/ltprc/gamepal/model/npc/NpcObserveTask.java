package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.config.GamePalConstants;

public class NpcObserveTask implements INpcTask{

    @Override
    public int getNpcTaskType() {
        return GamePalConstants.NPC_TASK_TYPE_OBSERVE;
    }
}
