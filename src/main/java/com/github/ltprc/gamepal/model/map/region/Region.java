package com.github.ltprc.gamepal.model.map.region;

import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.scene.Scene;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region extends RegionInfo{
    private Map<IntegerCoordinate, Scene> scenes = new ConcurrentHashMap<>(); // sceneCoordinate, scene
    private Map<IntegerCoordinate, Integer> terrainMap = new HashMap<>();
    private Map<IntegerCoordinate, BigDecimal> altitudeMap = new HashMap<>();
}
