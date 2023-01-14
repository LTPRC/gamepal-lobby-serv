package com.github.ltprc.gamepal.model.map;

import java.math.BigDecimal;

public class UserCoordinate {
    private String userCode;
    private int sceneNo;
    private Coordinate position;
    private Coordinate speed;
    private BigDecimal faceDirection; // from 0 to 360
}
