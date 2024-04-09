package com.github.ltprc.gamepal.model.map;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MovingBlock extends Block {
    private Coordinate speed;
    private BigDecimal faceDirection; // from 0 to 360

    public MovingBlock(Coordinate speed, BigDecimal faceDirection) {
        this.speed = new Coordinate(speed);
        this.faceDirection = faceDirection;
    }

    public MovingBlock(MovingBlock movingBlock) {
        super(movingBlock);
        speed = new Coordinate(movingBlock.speed);
        faceDirection = movingBlock.faceDirection;
    }
}
