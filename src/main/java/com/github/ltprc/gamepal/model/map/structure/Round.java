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
public class Round extends Shape {
    private int shapeType = GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND;
    private BigDecimal radius;

    public Round(BigDecimal radius) {
        this.radius = radius;
    }

    public Round(BigDecimal radius, Coordinate center) {
        this.radius = radius;
        this.center = new Coordinate(center.getX(), center.getY());
    }
}
