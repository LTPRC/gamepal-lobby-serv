package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.EventInfo;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface EventManager {

    EventInfo createEventInfo(final int eventCode, final String eventId);
    void addEvent(GameWorld world, int eventCode, String eventId, WorldCoordinate worldCoordinate);
    void updateEvents(GameWorld world);
}
