package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.model.map.coordinate.PlanarCoordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Structure {
    private Integer material;
    private Integer layer;
    private Shape shape = new Shape();
    private PlanarCoordinate imageSize = new PlanarCoordinate(BigDecimal.ONE, BigDecimal.valueOf(2));

    public Structure(Integer material, Integer layer) {
        this.material = material;
        this.layer = layer;
    }

    public Structure(Integer material, Integer layer, Shape shape) {
        this(material, layer);
        this.shape = new Shape(shape);
    }

    public Structure(Integer material, Integer layer, Shape shape, PlanarCoordinate imageSize) {
        this(material, layer, shape);
        this.imageSize = new PlanarCoordinate(imageSize);
    }
}
