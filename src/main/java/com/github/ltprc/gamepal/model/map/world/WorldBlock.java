package com.github.ltprc.gamepal.model.map.world;

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

    public WorldBlock(WorldBlock worldBlock) {
        super(worldBlock);
        type = worldBlock.type;
        id = worldBlock.id;
        code = worldBlock.code;
    }

    public WorldBlock(Integer type, String id, String code, WorldCoordinate worldCoordinate) {
        super(worldCoordinate);
        this.type = type;
        this.id = id;
        this.code = code;
    }
}
