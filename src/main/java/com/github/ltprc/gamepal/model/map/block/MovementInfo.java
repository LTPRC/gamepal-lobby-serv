package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.MovementConstants;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MovementInfo {
    private Coordinate speed = new Coordinate();
    private BigDecimal maxPlanarSpeed = MovementConstants.MAX_PLANAR_SPEED_DEFAULT; // block per frame
    private BigDecimal planarAcceleration = MovementConstants.MAX_PLANAR_SPEED_DEFAULT
            .multiply(MovementConstants.MAX_PLANAR_ACCELERATION_SPEED_RATIO); // block per frame square
    private BigDecimal faceDirection = MovementConstants.FACE_DIRECTION_DEFAULT; // from 0 to 360
    private int floorCode = MovementConstants.FLOOR_CODE_DEFAULT;
    private BigDecimal maxVerticalSpeed = MovementConstants.MAX_VERTICAL_SPEED_DEFAULT;
    private BigDecimal verticalAcceleration = MovementConstants.VERTICAL_ACCELERATION_DEFAULT;

    public MovementInfo(Coordinate speed, BigDecimal maxPlanarSpeed, BigDecimal planarAcceleration, BigDecimal faceDirection,
                        int floorCode, BigDecimal maxVerticalSpeed, BigDecimal verticalAcceleration) {
        this.speed = new Coordinate(speed);
        this.maxPlanarSpeed = maxPlanarSpeed;
        this.planarAcceleration = planarAcceleration;
        this.faceDirection = faceDirection;
        this.floorCode = floorCode;
        this.maxVerticalSpeed = maxVerticalSpeed;
        this.verticalAcceleration = verticalAcceleration;
    }

    public MovementInfo(MovementInfo movementInfo) {
        if (null == movementInfo) {
            return;
        }
        speed = new Coordinate(movementInfo.speed);
        maxPlanarSpeed = movementInfo.maxPlanarSpeed;
        planarAcceleration = movementInfo.planarAcceleration;
        faceDirection = movementInfo.faceDirection;
        floorCode = movementInfo.floorCode;
        maxVerticalSpeed = movementInfo.maxVerticalSpeed;
        verticalAcceleration = movementInfo.verticalAcceleration;
    }
}
