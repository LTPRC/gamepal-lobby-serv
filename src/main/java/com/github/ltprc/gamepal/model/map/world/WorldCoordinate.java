package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import lombok.Data;

@Data
public class WorldCoordinate {
    private int regionNo;
    private IntegerCoordinate sceneCoordinate;
    private Coordinate coordinate;
}
