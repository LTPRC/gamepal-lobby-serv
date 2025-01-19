package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class SkillConstants {

    private SkillConstants() {}

    public static final int SKILL_LENGTH = 4;

    public static final int SKILL_CODE_BLOCK = 3;
    public static final int SKILL_CODE_HEAL = 4;
    public static final int SKILL_CODE_CURSE = 5;
    public static final int SKILL_CODE_CHEER = 6;
    public static final int SKILL_CODE_MELEE_HIT = 10;
    public static final int SKILL_CODE_MELEE_KICK = 12;
    public static final int SKILL_CODE_MELEE_SCRATCH = 13;
    public static final int SKILL_CODE_MELEE_SMASH = 11;
    public static final int SKILL_CODE_MELEE_CLEAVE = 14;
    public static final int SKILL_CODE_MELEE_CHOP = 33;
    public static final int SKILL_CODE_MELEE_PICK = 36;
    public static final int SKILL_CODE_MELEE_STAB = 15;
    public static final int SKILL_CODE_SHOOT_HIT = 21;
    public static final int SKILL_CODE_SHOOT_ARROW = 22;
    public static final int SKILL_CODE_SHOOT_GUN = 23;
    public static final int SKILL_CODE_SHOOT_SHOTGUN = 24;
    public static final int SKILL_CODE_SHOOT_MAGNUM = 25;
    public static final int SKILL_CODE_SHOOT_ROCKET = 26;
    public static final int SKILL_CODE_SHOOT_FIRE = 27;
    public static final int SKILL_CODE_SHOOT_WATER = 28;
    public static final int SKILL_CODE_BUILD = 32;
    public static final int SKILL_CODE_FISH = 34;
    public static final int SKILL_CODE_SHOVEL = 35;
    public static final int SKILL_CODE_PLOW = 37;
    public static final int SKILL_CODE_LAY_MINE = 41;
    public static final int SKILL_CODE_LAY_BARRIER = 42;
    public static final int SKILL_CODE_LAY_WIRE_NETTING = 43;

    public static final int SKILL_MODE_SEMI_AUTO = 0;
    public static final int SKILL_MODE_AUTO = 1;

    public static final int SKILL_DEFAULT_FRAME = 25;

    public static final int SKILL_TYPE_DEFAULT = 0;
    public static final int SKILL_TYPE_ATTACK = 1;

    // Backend constants

    public static final BigDecimal SKILL_RANGE_MELEE = BigDecimal.ONE;
    public static final BigDecimal SKILL_ANGLE_MELEE_MAX = BigDecimal.valueOf(120D);
    public static final BigDecimal SKILL_RANGE_SHOOT = BigDecimal.valueOf(10);
    public static final BigDecimal SKILL_ANGLE_SHOOT_MAX = BigDecimal.valueOf(30D);
    public static final BigDecimal SKILL_RANGE_SHOOT_SHOTGUN = BigDecimal.valueOf(5);
    public static final BigDecimal SKILL_ANGLE_SHOOT_SHOTGUN_MAX = BigDecimal.valueOf(60D);
    public static final BigDecimal SKILL_RANGE_SHOOT_FIRE_MIN = BigDecimal.valueOf(1);
    public static final BigDecimal SKILL_RANGE_SHOOT_FIRE_MAX = BigDecimal.valueOf(5);
    public static final BigDecimal SKILL_RANGE_SHOOT_WATER = BigDecimal.ONE;
    public static final BigDecimal SKILL_RANGE_EXPLODE = BigDecimal.valueOf(2);
    public static final BigDecimal SKILL_RANGE_CURSE = BigDecimal.valueOf(5);
    public static final BigDecimal SKILL_RANGE_CHEER = BigDecimal.valueOf(5);
    public static final BigDecimal SKILL_RANGE_MINE = BigDecimal.ONE;
    public static final BigDecimal SKILL_RANGE_FIRE = BigDecimal.valueOf(0.5D);
    public static final BigDecimal SKILL_RANGE_BUILD = BigDecimal.ONE;
}
