package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface EventManager {

    MovementInfo createMovementInfoByEventCode(final int eventCode);
    void addEvent(GameWorld world, int eventCode, String eventId, WorldCoordinate worldCoordinate);
    void updateEvents(GameWorld world);
}
