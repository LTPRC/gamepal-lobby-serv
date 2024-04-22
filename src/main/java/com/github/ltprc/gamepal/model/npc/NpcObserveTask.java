package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.config.PlayerConstants;

public class NpcObserveTask implements INpcTask{

    @Override
    public int getNpcTaskType() {
        return PlayerConstants.NPC_TASK_TYPE_OBSERVE;
    }
}
