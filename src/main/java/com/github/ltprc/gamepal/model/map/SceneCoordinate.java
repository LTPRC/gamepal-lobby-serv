package com.github.ltprc.gamepal.model.map;

import lombok.Data;

@Data
public abstract class SceneCoordinate {
    public static String detectionType;
    private int worldNo; // TBD
    private int sceneNo;
    private Coordinate position;
    private String userCode;
}
