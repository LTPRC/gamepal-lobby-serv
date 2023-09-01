package com.github.ltprc.gamepal.model.map;

import lombok.Data;

import java.util.Map;

@Data
public class Region extends RegionInfo{
    private Map<IntegerCoordinate, Scene> scenes; // sceneCoordinate, scene
}
