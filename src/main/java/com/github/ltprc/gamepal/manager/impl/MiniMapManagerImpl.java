package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSONArray;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.manager.MiniMapManager;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;
import org.springframework.stereotype.Component;

import java.awt.*;


@Component
public class MiniMapManagerImpl implements MiniMapManager {

    @Override
    public JSONArray generateMiniMapBackground(Region region, IntegerCoordinate miniMapSize) {
        JSONArray rst = new JSONArray();
        for (int i = 0; i < miniMapSize.getX(); i++) {
            for (int j = 0; j < miniMapSize.getY(); j++) {
                Color color = BlockConstants.BLOCK_CODE_COLOR_MAP.get(
                        region.getTerrainMap().getOrDefault(new IntegerCoordinate(
                                -region.getRadius() + (region.getRadius() * 2 + 1) * i / miniMapSize.getX(),
                                -region.getRadius() + (region.getRadius() * 2 + 1) * j / miniMapSize.getY()),
                                BlockConstants.BLOCK_CODE_BLACK));
                rst.add(color);
            }
        }
        return rst;
    }

    @Override
    public IntegerCoordinate getMiniMapSceneCoordinate(Region region, IntegerCoordinate miniMapSize,
                                                       IntegerCoordinate sceneCoordinate) {
        return new IntegerCoordinate(
                miniMapSize.getX() / 2 + sceneCoordinate.getX() * miniMapSize.getX() / 2 / region.getRadius(),
                miniMapSize.getY() / 2 + sceneCoordinate.getY() * miniMapSize.getY() / 2 / region.getRadius());
    }
}
