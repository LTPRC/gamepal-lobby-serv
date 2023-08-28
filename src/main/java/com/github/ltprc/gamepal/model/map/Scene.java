package com.github.ltprc.gamepal.model.map;

import lombok.Data;

import java.util.Map;

@Data
public class Scene {
    private String name;
    private int y;
    private int x;
    private Map<IntegerCoordinate, Integer> blocks;
    private Map<IntegerCoordinate, Integer> terrain;
}
