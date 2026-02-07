package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class MovementConstants {

    // Backend constants

    public static final BigDecimal MIN_BLOCK_ACCELERATION = BigDecimal.valueOf(0.0001D);
    public static final BigDecimal MIN_BLOCK_SPEED = BigDecimal.valueOf(0.001D);
    public static final BigDecimal MAX_PLANAR_SPEED_DEFAULT = BigDecimal.valueOf(0.2D);
    public static final BigDecimal MAX_PLANAR_ACCELERATION_SPEED_RATIO = BigDecimal.valueOf(0.1D);
    public static final BigDecimal FACE_DIRECTION_DEFAULT = BigDecimal.ZERO;
    public static final int FLOOR_CODE_DEFAULT = BlockConstants.BLOCK_CODE_BLACK;
//    public static final BigDecimal VERTICAL_ACCELERATION_DEFAULT = BigDecimal.valueOf(-0.01D);
//    public static final BigDecimal VERTICAL_ACCELERATION_PARACHUTE = BigDecimal.valueOf(-0.004D);
//    public static final BigDecimal MAX_VERTICAL_SPEED_DEFAULT = BigDecimal.valueOf(0.16D); // Positive but downward
//    public static final BigDecimal MAX_VERTICAL_SPEED_PARACHUTE = BigDecimal.valueOf(0.1D); // Positive but downward
    public static final BigDecimal VERTICAL_ACCELERATION_DEFAULT = BigDecimal.valueOf(-0.4D);
    public static final BigDecimal VERTICAL_ACCELERATION_PARACHUTE = BigDecimal.valueOf(-0.1D);
    public static final BigDecimal MAX_VERTICAL_SPEED_DEFAULT = BigDecimal.valueOf(25); // Positive but downward
    public static final BigDecimal MAX_VERTICAL_SPEED_PARACHUTE = BigDecimal.valueOf(2); // Positive but downward
    public static final BigDecimal MAX_VERTICAL_STEP_DEFAULT = BigDecimal.valueOf(0.5D);
    public static final BigDecimal MIN_CREATURE_INJURIOUS_SPEED_DEFAULT = BigDecimal.valueOf(0.5D);
    public static final BigDecimal MIN_CREATURE_LETHAL_SPEED_DEFAULT = BigDecimal.valueOf(5);
    public static final BigDecimal FRICTION_FACTOR = BigDecimal.valueOf(0.9D);

    public static final int MOVEMENT_MODE_DEFAULT = 0;
    public static final int MOVEMENT_MODE_STAND_GROUND = 1;
    public static final int MOVEMENT_MODE_WALK = 2;
}
