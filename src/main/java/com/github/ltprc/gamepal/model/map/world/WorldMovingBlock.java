package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldMovingBlock extends WorldBlock {
    private Coordinate speed;
    private BigDecimal faceDirection; // from 0 to 360

    public WorldMovingBlock(WorldMovingBlock worldMovingBlock) {
        super(worldMovingBlock);
        speed = worldMovingBlock.speed;
        faceDirection = worldMovingBlock.faceDirection;
    }
}
