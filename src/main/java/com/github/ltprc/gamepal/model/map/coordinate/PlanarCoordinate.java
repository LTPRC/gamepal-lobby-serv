package com.github.ltprc.gamepal.model.map.coordinate;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlanarCoordinate {
    private BigDecimal x;
    private BigDecimal y;

    public PlanarCoordinate() {
        x = BigDecimal.ZERO;
        y = BigDecimal.ZERO;
    }

    public PlanarCoordinate(PlanarCoordinate coordinate) {
        x = coordinate.x;
        y = coordinate.y;
    }
}
