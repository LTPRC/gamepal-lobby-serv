package com.github.ltprc.gamepal.model.map.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameWorldInfo {

    private int worldTime;
    private BigDecimal windDirection;
    private BigDecimal windSpeed;
}
