package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region extends RegionInfo{
    private Map<IntegerCoordinate, Scene> scenes; // sceneCoordinate, scene
}
