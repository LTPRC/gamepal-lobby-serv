package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Block extends Coordinate {
    private int type;
    private String id;
    private String code;
    private Structure structure;

    public Block(Block block) {
        super(block);
        type = block.type;
        id = block.id;
        code = block.code;
        structure = block.structure;
    }

    public Block(Integer type, String id, String code, Structure structure, Coordinate coordinate) {
        super(coordinate);
        this.type = type;
        this.id = id;
        this.code = code;
        this.structure = new Structure(structure.getMaterial(), structure.getLayer(), structure.getShape(),
                structure.getImageSize());
    }
}
