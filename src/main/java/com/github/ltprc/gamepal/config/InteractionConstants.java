package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class InteractionConstants {

    private InteractionConstants() {}

    // Frontend constants
    public static final int INTERACTION_USE = 0;
    public static final int INTERACTION_EXCHANGE = 1;
    public static final int INTERACTION_SLEEP = 2;
    public static final int INTERACTION_DRINK = 3;
    public static final int INTERACTION_DECOMPOSE = 4;
    public static final int INTERACTION_TALK = 5;
    public static final int INTERACTION_ATTACK = 6;
    public static final int INTERACTION_FLIRT = 7;
    public static final int INTERACTION_SET = 8;
    public static final int INTERACTION_SUCCUMB = 9;
    public static final int INTERACTION_EXPEL = 10;
    public static final int INTERACTION_PACK = 11;
    public static final int INTERACTION_PLANT = 12;
    public static final int INTERACTION_GATHER = 13;
    public static final int INTERACTION_PULL = 14;

    // Backend constants

    public static final BigDecimal MAX_INTERACTION_DISTANCE = BigDecimal.valueOf(2.5D);
    public static final BigDecimal MAX_INTERACTION_ANGLE = BigDecimal.valueOf(30D);
}
