package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlockInfo {
    int type;
    String id;
    String code;
    Structure structure;

    public BlockInfo(BlockInfo block) {
        type = block.type;
        id = block.id;
        code = block.code;
        structure = block.structure;
    }

    public BlockInfo(Integer type, String id, String code, Structure structure) {
        this.type = type;
        this.id = id;
        this.code = code;
        this.structure = new Structure(structure.getMaterial(), structure.getLayer(), structure.getShape(),
                structure.getImageSize());
    }
}
