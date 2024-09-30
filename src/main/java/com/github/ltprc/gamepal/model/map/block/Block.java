package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import lombok.Data;

@Data
public class Block {

    private WorldCoordinate worldCoordinate;
    private BlockInfo blockInfo;
    private MovementInfo movementInfo;
    private PlayerInfo playerInfo;

    public Block(Block worldBlock) {
        worldCoordinate = new WorldCoordinate(worldBlock.worldCoordinate);
        blockInfo = new BlockInfo(worldBlock.blockInfo);
        movementInfo = new MovementInfo(worldBlock.movementInfo);
        playerInfo = worldBlock.playerInfo; // Shallow copy
    }

    public Block(WorldCoordinate worldCoordinate, BlockInfo blockInfo, MovementInfo movementInfo) {
        this.worldCoordinate = new WorldCoordinate(worldCoordinate);
        this.blockInfo = new BlockInfo(blockInfo);
        this.movementInfo = new MovementInfo(movementInfo);
    }

    public Block(WorldCoordinate worldCoordinate, BlockInfo blockInfo, MovementInfo movementInfo,
                 PlayerInfo playerInfo) {
        this(worldCoordinate, blockInfo, movementInfo);
        this.playerInfo = playerInfo; // Shallow copy
    }
}
