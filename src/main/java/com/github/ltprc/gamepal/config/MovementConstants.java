package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class MovementConstants {

    // Backend constants

    public static final BigDecimal MAX_SPEED_DEFAULT = BigDecimal.valueOf(0.2D);
    public static final BigDecimal ACCELERATION_MAX_SPEED_RATIO = BigDecimal.valueOf(0.1D);
    public static final BigDecimal FACE_DIRECTION_DEFAULT = BigDecimal.ZERO;
    public static final int FLOOR_CODE_DEFAULT = BlockConstants.BLOCK_CODE_BLACK;
    public static final BigDecimal ACCELERATION_Z_DEFAULT = BigDecimal.valueOf(0.01D);
    public static final BigDecimal ACCELERATION_Z_PARACHUTE = BigDecimal.valueOf(0.004D);
    public static final BigDecimal MAX_SPEED_Z_DEFAULT = BigDecimal.valueOf(0.16D);
    public static final BigDecimal MAX_SPEED_Z_PARACHUTE = BigDecimal.valueOf(0.1D);
    public static final BigDecimal MAX_Z_STEP_DEFAULT = BigDecimal.valueOf(0.5D);

    public static final int MOVEMENT_MODE_DEFAULT = 0;
    public static final int MOVEMENT_MODE_STAND_GROUND = 1;
    public static final int MOVEMENT_MODE_WALK = 2;
}
