package com.github.ltprc.gamepal.manager;

import com.alibaba.fastjson.JSONArray;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;

public interface GameMapManager {

    JSONArray generateMiniMapBackground(Region region, IntegerCoordinate miniMapSize);

    IntegerCoordinate getMiniMapSceneCoordinate(Region region, IntegerCoordinate miniMapSize,
                                                IntegerCoordinate sceneCoordinate);
}
