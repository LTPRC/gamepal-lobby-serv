package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.BlockConstants;
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
    private int frame;
    private int frameMax;
    private int period;

    public MovementInfo() {
        speed = new Coordinate();
        maxSpeed = BlockConstants.MAX_SPEED_DEFAULT;
        acceleration = BlockConstants.ACCELERATION_DEFAULT;
        faceDirection = BlockConstants.FACE_DIRECTION_DEFAULT;
        floorCode = BlockConstants.FLOOR_CODE_DEFAULT;
        frame = BlockConstants.FRAME_DEFAULT;
        frameMax = BlockConstants.FRAME_MAX_DEFAULT;
        period = BlockConstants.PERIOD_DEFAULT;
    }

    public MovementInfo(Coordinate speed, BigDecimal maxSpeed, BigDecimal acceleration, BigDecimal faceDirection,
                        int floorCode, int frame, int frameMax, int period) {
        this.speed = new Coordinate(speed);
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.faceDirection = faceDirection;
        this.floorCode = floorCode;
        this.frame = frame;
        this.frameMax = frameMax;
        this.period = period;
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
        frame = movementInfo.frame;
        frameMax = movementInfo.frameMax;
        period = movementInfo.period;
    }
}
