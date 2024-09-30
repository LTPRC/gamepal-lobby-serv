package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import lombok.Data;

@Data
public class BlockOld {

    private Coordinate coordinate;
    private BlockInfo blockInfo;
    private MovementInfo movementInfo;

    public BlockOld(BlockOld block) {
        coordinate = new Coordinate(block.coordinate);
        blockInfo = new BlockInfo(block.blockInfo);
        movementInfo = new MovementInfo(block.movementInfo);
    }

    public BlockOld(Coordinate coordinate, BlockInfo blockInfo, MovementInfo movementInfo) {
        this.coordinate = new Coordinate(coordinate);
        this.blockInfo = new BlockInfo(blockInfo);
        this.movementInfo = new MovementInfo(movementInfo);
    }
}
