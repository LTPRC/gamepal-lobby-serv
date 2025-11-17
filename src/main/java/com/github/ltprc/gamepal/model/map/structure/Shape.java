package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shape {
    // Default square
    private int shapeType = BlockConstants.STRUCTURE_SHAPE_TYPE_SQUARE;
    Coordinate radius = new Coordinate(BigDecimal.valueOf(0.5D), BigDecimal.valueOf(0.5D), BlockConstants.Z_DEFAULT);

    public Shape(Shape shape) {
        shapeType = shape.shapeType;
        radius = new Coordinate(shape.radius);
    }
}
