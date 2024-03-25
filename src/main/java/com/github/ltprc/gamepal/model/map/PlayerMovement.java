package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMovement extends WorldCoordinate {
    private Coordinate speed;
    private BigDecimal faceDirection; // from 0 to 360
    private int playerStatus;
}
