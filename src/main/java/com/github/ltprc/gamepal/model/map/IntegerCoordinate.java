package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegerCoordinate {
    private Integer x;
    private Integer y;

    public IntegerCoordinate(IntegerCoordinate integerCoordinate) {
        if (null == integerCoordinate) {
            return;
        }
        x = integerCoordinate.getX().intValue();
        y = integerCoordinate.getY().intValue();
    }
}
