package com.github.ltprc.gamepal.model.map;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MovingBlock extends Block {
    private Coordinate speed;
    private BigDecimal maxSpeed; // block per frame
    private BigDecimal acceleration; // block per frame square
    private BigDecimal faceDirection; // from 0 to 360
    private int floorCode;

    public MovingBlock(Coordinate speed, BigDecimal faceDirection) {
        this.speed = new Coordinate(speed);
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.faceDirection = faceDirection;
        this.floorCode = floorCode;
    }

    public MovingBlock(MovingBlock movingBlock) {
        super(movingBlock);
        speed = new Coordinate(movingBlock.speed);
        maxSpeed = movingBlock.maxSpeed;
        acceleration = movingBlock.acceleration;
        faceDirection = movingBlock.faceDirection;
        floorCode = movingBlock.floorCode;
    }
}
