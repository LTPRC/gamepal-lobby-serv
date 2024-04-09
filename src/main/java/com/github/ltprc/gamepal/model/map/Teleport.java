package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Teleport extends Block {
    private WorldCoordinate to;

    public Teleport(Teleport teleport) {
        super(teleport);
        to = new WorldCoordinate(teleport.to);
    }

    public Teleport(WorldCoordinate to, Block block) {
        super(block);
        this.to = new WorldCoordinate(to);
    }
}
