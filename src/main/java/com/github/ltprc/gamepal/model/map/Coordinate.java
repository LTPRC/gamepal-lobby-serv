package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Coordinate {
    private BigDecimal x;
    private BigDecimal y;

    public Coordinate() {
        x = BigDecimal.ZERO;
        y = BigDecimal.ZERO;
    }

    public Coordinate(Coordinate coordinate) {
        x = coordinate.x;
        y = coordinate.y;
    }
}
