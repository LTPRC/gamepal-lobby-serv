package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teleport extends Block {
    private WorldCoordinate to;

    public Teleport(Teleport teleport) {
        super(teleport);
        to = teleport.to;
    }

    public Teleport(WorldCoordinate to, Block block) {
        super(block);
        this.to = to;
    }
}
