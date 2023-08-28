package com.github.ltprc.gamepal.model.map;

import lombok.Data;

@Data
public abstract class RegionCoordinate {
    private int regionNo;
    private IntegerCoordinate sceneCoordinate;
    private Coordinate coordinate;
}
