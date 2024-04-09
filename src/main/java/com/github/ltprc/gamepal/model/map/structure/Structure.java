package com.github.ltprc.gamepal.model.map.structure;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Structure {
    private Integer material; // 0-hollow 1-solid 2-flesh
    private Integer layer; // 10-bottom 20-bottom-decoration 30-middle 40-middle-decoration 50-top 60-top-decoration
    private List<Shape> shapes = new ArrayList<>();
    private Coordinate imageSize = new Coordinate(BigDecimal.ONE, BigDecimal.ONE);

    public Structure(Integer material, Integer layer) {
        this.material = material;
        this.layer = layer;
    }

    public Structure(Integer material, Integer layer, BigDecimal imageWidth, BigDecimal imageHeight) {
        this.material = material;
        this.layer = layer;
        this.imageSize = new Coordinate(imageWidth, imageHeight);
    }

    public Structure(Structure structure) {
        material = structure.material;
        layer = structure.layer;
        shapes = new ArrayList<>(structure.shapes);
        imageSize = new Coordinate(structure.imageSize);
    }
}
