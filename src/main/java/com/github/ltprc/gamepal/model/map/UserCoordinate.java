package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCoordinate {
    private SceneModel scenes;
    private Coordinate position;
    private Coordinate speed;
    private BigDecimal faceDirection; // from 0 to 360
}
