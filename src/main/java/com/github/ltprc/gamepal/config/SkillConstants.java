package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class SkillConstants {

    private SkillConstants() {}

    public static final int SKILL_LENGTH = 4;

    public static final int SKILL_CODE_BLOCK = 3;
    public static final int SKILL_CODE_HEAL = 4;
    public static final int SKILL_CODE_CURSE = 5;
    public static final int SKILL_CODE_CHEER = 6;
    public static final int SKILL_CODE_MELEE_HIT = 11;
    public static final int SKILL_CODE_MELEE_KICK = 12;
    public static final int SKILL_CODE_MELEE_SCRATCH = 13;
    public static final int SKILL_CODE_MELEE_CLEAVE = 14;
    public static final int SKILL_CODE_MELEE_STAB = 15;
    public static final int SKILL_CODE_SHOOT_HIT = 21;
    public static final int SKILL_CODE_SHOOT_ARROW = 22;
    public static final int SKILL_CODE_SHOOT_GUN = 23;
    public static final int SKILL_CODE_SHOOT_SHOTGUN = 24;
    public static final int SKILL_CODE_SHOOT_MAGNUM = 25;
    public static final int SKILL_CODE_SHOOT_ROCKET = 26;

    public static final int SKILL_MODE_SEMI_AUTO = 0;
    public static final int SKILL_MODE_AUTO = 1;

    public static final int SKILL_DEFAULT_FRAME = 25;

    public static final int SKILL_TYPE_DEFAULT = 0;
    public static final int SKILL_TYPE_ATTACK = 1;

    // Backend constants

    public static final BigDecimal SKILL_RANGE_CURSE = BigDecimal.valueOf(5);
    public static final BigDecimal SKILL_RANGE_CHEER = BigDecimal.valueOf(5);
}
