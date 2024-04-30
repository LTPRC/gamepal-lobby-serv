package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class WorldMovingBlock extends WorldBlock {
    private Coordinate speed;
    private BigDecimal maxSpeed; // block per frame
    private BigDecimal acceleration; // block per frame square
    private BigDecimal faceDirection; // from 0 to 360

    public WorldMovingBlock(Coordinate speed, BigDecimal maxSpeed, BigDecimal acceleration, BigDecimal faceDirection) {
        this.speed = new Coordinate(speed);
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.faceDirection = faceDirection;
    }

    public WorldMovingBlock(WorldMovingBlock worldMovingBlock) {
        super(worldMovingBlock);
        speed = worldMovingBlock.speed;
        maxSpeed = worldMovingBlock.maxSpeed;
        acceleration = worldMovingBlock.acceleration;
        faceDirection = worldMovingBlock.faceDirection;
    }
}
