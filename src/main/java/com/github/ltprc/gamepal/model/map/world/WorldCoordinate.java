package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorldCoordinate {
    private int regionNo;
    private IntegerCoordinate sceneCoordinate;
    private Coordinate coordinate;

    public WorldCoordinate() {
        regionNo = 0;
        sceneCoordinate = new IntegerCoordinate(0, 0);
        coordinate = new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public WorldCoordinate(WorldCoordinate worldCoordinate) {
        if (null == worldCoordinate) {
            return;
        }
        this.setRegionNo(worldCoordinate.getRegionNo());
        this.setSceneCoordinate(new IntegerCoordinate(worldCoordinate.getSceneCoordinate()));
        this.setCoordinate(new Coordinate(worldCoordinate.getCoordinate()));
    }

    public WorldCoordinate(int regionNo, IntegerCoordinate sceneCoordinate, Coordinate coordinate) {
        this.regionNo = regionNo;
        this.sceneCoordinate = new IntegerCoordinate(sceneCoordinate);
        this.coordinate = new Coordinate(coordinate);
    }
}
