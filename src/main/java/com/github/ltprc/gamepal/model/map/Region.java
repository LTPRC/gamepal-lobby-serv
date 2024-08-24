package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region extends RegionInfo{
    private Map<IntegerCoordinate, Scene> scenes = new ConcurrentHashMap<>(); // sceneCoordinate, scene
    private Map<IntegerCoordinate, Integer> terrainMap = new HashMap<>();
    private Map<IntegerCoordinate, Double> altitudeMap = new HashMap<>();
}
