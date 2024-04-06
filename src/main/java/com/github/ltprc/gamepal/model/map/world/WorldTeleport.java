package com.github.ltprc.gamepal.model.map.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldTeleport extends WorldBlock {
    private WorldCoordinate to;

    public WorldTeleport(WorldTeleport worldTeleport) {
        super(worldTeleport);
        to = worldTeleport.to;
    }

    public WorldTeleport(WorldCoordinate to, WorldBlock worldBlock) {
        super(worldBlock);
        this.to = to;
    }
}
