package com.github.ltprc.gamepal.model.map.world;

import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.Data;

import java.math.BigDecimal;

//@Data
@Deprecated
public class WorldMovingBlock {
    private Coordinate speed;
    private BigDecimal maxSpeed; // block per frame
    private BigDecimal acceleration; // block per frame square
    private BigDecimal faceDirection; // from 0 to 360
    private int floorCode;

//    public WorldMovingBlock(Coordinate speed, BigDecimal maxSpeed, BigDecimal acceleration, BigDecimal faceDirection,
//                            int floorCode) {
//        this.speed = new Coordinate(speed);
//        this.maxSpeed = maxSpeed;
//        this.acceleration = acceleration;
//        this.faceDirection = faceDirection;
//        this.floorCode = floorCode;
//    }

    public WorldMovingBlock(WorldMovingBlock worldMovingBlock) {
//        super(worldMovingBlock);
        speed = new Coordinate(worldMovingBlock.speed);
        maxSpeed = worldMovingBlock.maxSpeed;
        acceleration = worldMovingBlock.acceleration;
        faceDirection = worldMovingBlock.faceDirection;
        floorCode = worldMovingBlock.floorCode;
    }
}
