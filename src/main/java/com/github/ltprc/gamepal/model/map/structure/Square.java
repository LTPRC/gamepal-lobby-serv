package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Square extends Shape {
    private int shapeType = GamePalConstants.STRUCTURE_SHAPE_TYPE_SQUARE;
    private BigDecimal sideLength;

    public Square(BigDecimal sideLength) {
        this.sideLength = sideLength;
    }

    public Square(BigDecimal sideLength, Coordinate center) {
        this.sideLength = sideLength;
        this.center = new Coordinate(center);
    }
}
