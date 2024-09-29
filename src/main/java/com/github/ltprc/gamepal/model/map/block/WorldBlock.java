package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorldBlock extends WorldCoordinate {
    private int type;
    private String id;
    private String code;
    private Structure structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
            BlockConstants.STRUCTURE_LAYER_MIDDLE);

    public WorldBlock(WorldBlock worldBlock) {
        super(worldBlock);
        type = worldBlock.type;
        id = worldBlock.id;
        code = worldBlock.code;
        structure = worldBlock.structure;
    }

    public WorldBlock(Integer type, String id, String code, Structure structure, WorldCoordinate worldCoordinate) {
        super(worldCoordinate);
        this.type = type;
        this.id = id;
        this.code = code;
        this.structure = new Structure(structure.getMaterial(), structure.getLayer(), structure.getShape(),
                structure.getImageSize());
    }
}
