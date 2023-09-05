package com.github.ltprc.gamepal.model.map;

import lombok.Data;

import java.util.Map;

@Data
public class Scene extends SceneInfo {
    private Map<IntegerCoordinate, Integer> blocks;
}
