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
public class Rectangle extends Shape {
    private int shapeType = GamePalConstants.STRUCTURE_SHAPE_TYPE_RECTANGLE;
    private BigDecimal length;
    private BigDecimal width;

    public Rectangle(BigDecimal length, BigDecimal width) {
        this.length = length;
        this.width = width;
    }

    public Rectangle(BigDecimal length, BigDecimal width, Coordinate center) {
        this.length = length;
        this.width = width;
        this.center = new Coordinate(center.getX(), center.getY());
    }
}
