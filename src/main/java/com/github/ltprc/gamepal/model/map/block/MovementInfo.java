package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.model.map.Coordinate;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MovementInfo {
    private Coordinate speed;
    private BigDecimal maxSpeed; // block per frame
    private BigDecimal acceleration; // block per frame square
    private BigDecimal faceDirection; // from 0 to 360
    private int floorCode;

    public MovementInfo() {
        speed = new Coordinate();
        maxSpeed = CreatureConstants.MAX_SPEED_DEFAULT;
        acceleration = CreatureConstants.ACCELERATION_DEFAULT;
        faceDirection = CreatureConstants.FACE_DIRECTION_DEFAULT;
        floorCode = CreatureConstants.FLOOR_CODE_DEFAULT;
    }

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
