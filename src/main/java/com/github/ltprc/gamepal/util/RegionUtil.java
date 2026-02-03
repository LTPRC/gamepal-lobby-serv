package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;

public class RegionUtil {

    private RegionUtil() {}

    public static boolean validateSceneCoordinate(RegionInfo regionInfo, IntegerCoordinate sceneCoordinate) {
        return sceneCoordinate.getX() >= -regionInfo.getRadius()
                || sceneCoordinate.getX() <= regionInfo.getRadius()
                || sceneCoordinate.getY() >= -regionInfo.getRadius()
                || sceneCoordinate.getY() <= regionInfo.getRadius();
    }
}
