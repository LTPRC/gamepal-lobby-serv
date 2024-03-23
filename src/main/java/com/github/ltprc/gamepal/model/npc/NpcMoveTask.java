package com.github.ltprc.gamepal.model.npc;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class NpcMoveTask implements INpcTask{

    @Autowired
    private UserService userService;

    @Override
    public int getNpcTaskType() {
        return GamePalConstants.NPC_TASK_TYPE_MOVE;
    }

    @Override
    public void runNpcTask(String npcUserCode, WorldCoordinate wc) {
        GameWorld world = userService.getWorldByUserCode(npcUserCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(npcUserCode);
    }
}
