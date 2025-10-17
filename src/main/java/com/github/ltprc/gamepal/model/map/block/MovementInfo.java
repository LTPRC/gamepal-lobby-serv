package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MovementInfo {
    private Coordinate speed = new Coordinate();
    private BigDecimal maxSpeed = BlockConstants.MAX_SPEED_DEFAULT; // block per frame
    private BigDecimal acceleration = BlockConstants.MAX_SPEED_DEFAULT
            .multiply(BlockConstants.ACCELERATION_MAX_SPEED_RATIO); // block per frame square
    private BigDecimal faceDirection = BlockConstants.FACE_DIRECTION_DEFAULT; // from 0 to 360
    private int floorCode = BlockConstants.FLOOR_CODE_DEFAULT;

    public MovementInfo(Coordinate speed, BigDecimal maxSpeed, BigDecimal acceleration, BigDecimal faceDirection,
                        int floorCode) {
        this.speed = new Coordinate(speed);
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.faceDirection = faceDirection;
        this.floorCode = floorCode;
    }

    public MovementInfo(MovementInfo movementInfo) {
        if (null == movementInfo) {
            return;
        }
        speed = new Coordinate(movementInfo.speed);
        maxSpeed = movementInfo.maxSpeed;
        acceleration = movementInfo.acceleration;
        faceDirection = movementInfo.faceDirection;
        floorCode = movementInfo.floorCode;
    }
}
