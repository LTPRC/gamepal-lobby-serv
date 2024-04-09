package com.github.ltprc.gamepal.model.map.world;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorldTeleport extends WorldBlock {
    private WorldCoordinate to;

    public WorldTeleport(WorldTeleport worldTeleport) {
        super(worldTeleport);
        to = new WorldCoordinate(worldTeleport.to);
    }

    public WorldTeleport(WorldCoordinate to, WorldBlock worldBlock) {
        super(worldBlock);
        this.to = new WorldCoordinate(to);
    }
}
