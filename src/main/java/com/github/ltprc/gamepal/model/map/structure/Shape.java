package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Shape {
    private int shapeType;
    Coordinate center = new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO);
}
