package com.github.ltprc.gamepal.model.map.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldEvent extends WorldCoordinate {
    private String userCode;
    private int code;
    private int frame;
    private int frameMax;
    private int period;

    public WorldEvent(WorldCoordinate worldCoordinate) {
        super(worldCoordinate);
    }
}
