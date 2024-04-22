package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.config.PlayerConstants;

public class NpcMoveTask implements INpcTask{

    @Override
    public int getNpcTaskType() {
        return PlayerConstants.NPC_TASK_TYPE_MOVE;
    }
}
