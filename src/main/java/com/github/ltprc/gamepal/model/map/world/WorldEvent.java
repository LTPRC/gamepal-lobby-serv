package com.github.ltprc.gamepal.model.map.world;

@Deprecated
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
