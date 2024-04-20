package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldBlock extends WorldCoordinate {
    private Integer type;
    private String id;
    private String code;
    private Integer frame;
    private Structure structure;

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
