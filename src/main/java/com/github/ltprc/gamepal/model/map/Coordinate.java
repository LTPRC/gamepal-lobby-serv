package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    private BigDecimal x;
    private BigDecimal y;

    public Coordinate(Coordinate coordinate) {
        x = coordinate.x;
        y = coordinate.y;
    }
}
