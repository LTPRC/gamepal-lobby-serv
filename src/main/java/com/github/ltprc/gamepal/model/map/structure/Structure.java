package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Structure {
    private Integer material; // 0-hollow 1-solid 2-flesh
    private Integer layer; // 10-bottom 20-bottom-decoration 30-middle 40-middle-decoration 50-top 60-top-decoration
    private Shape shape = new Shape();
    private Coordinate imageSize = new Coordinate(BigDecimal.ONE, BigDecimal.ONE);

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
