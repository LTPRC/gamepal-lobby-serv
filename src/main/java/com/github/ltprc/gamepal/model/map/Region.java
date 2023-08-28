package com.github.ltprc.gamepal.model.map;

import lombok.Data;

import java.util.Map;

@Data
public class Region {
    private int regionNo;
    private int height;
    private int width;
    private Map<IntegerCoordinate, Scene> scenes; // sceneCoordinate, scene
}
