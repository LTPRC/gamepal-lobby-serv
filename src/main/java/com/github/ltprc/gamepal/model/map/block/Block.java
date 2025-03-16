package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import lombok.Data;

@Data
public class Block {

    private WorldCoordinate worldCoordinate;
    private BlockInfo blockInfo;
    private MovementInfo movementInfo;

    public Block(Block worldBlock) {
        worldCoordinate = new WorldCoordinate(worldBlock.worldCoordinate);
        blockInfo = new BlockInfo(worldBlock.blockInfo);
        movementInfo = new MovementInfo(worldBlock.movementInfo);
    }

    public Block(WorldCoordinate worldCoordinate, BlockInfo blockInfo, MovementInfo movementInfo) {
        this.worldCoordinate = new WorldCoordinate(worldCoordinate);
        this.blockInfo = new BlockInfo(blockInfo);
        this.movementInfo = new MovementInfo(movementInfo);
    }
}
