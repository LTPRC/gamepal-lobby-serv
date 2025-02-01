package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Structure {
    private Integer material;
    private Integer layer;
    private Shape shape = new Shape();
    private Coordinate imageSize = new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2));

    public Structure(Integer material, Integer layer) {
        this.material = material;
        this.layer = layer;
    }

    public Structure(Integer material, Integer layer, Shape shape) {
        this(material, layer);
        this.shape = new Shape(shape);
    }

    public Structure(Integer material, Integer layer, Shape shape, Coordinate imageSize) {
        this(material, layer, shape);
        this.imageSize = new Coordinate(imageSize);
    }
}
