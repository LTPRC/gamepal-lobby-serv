package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class CreatureConstants {

    private CreatureConstants() {}

    public static final int PLAYER_TYPE_HUMAN = 0;
    public static final int PLAYER_TYPE_NPC = 1;
    public static final int CREATURE_TYPE_HUMAN = 1;
    public static final int CREATURE_TYPE_ANIMAL = 2;
    public static final int FACE_COEFS_LENGTH = 14;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    // Backend constants

    public static final int SKIN_COLOR_PAOFU = 1;
    public static final int SKIN_COLOR_FROG = 2;
    public static final int SKIN_COLOR_MONKEY = 3;
    public static final int SKIN_COLOR_RACOON = 4;
    public static final int SKIN_COLOR_CHICKEN = 5;
    public static final int SKIN_COLOR_BUFFALO = 6;
    public static final int SKIN_COLOR_FOX = 7;
    public static final int SKIN_COLOR_POLAR_BEAR = 8;
    public static final int SKIN_COLOR_SHEEP = 9;
    public static final int SKIN_COLOR_TIGER = 10;
    public static final int SKIN_COLOR_CAT = 11;
    public static final int SKIN_COLOR_DOG = 12;
    public static final int SKIN_COLOR_WOLF = 13;
    public static final int SKIN_COLOR_BOAR = 14;
    public static final int SKIN_COLOR_HORSE = 15;

    public static final int NPC_BRAIN_STATUS_UNAWARE = 0;
    public static final int NPC_BRAIN_STATUS_SUSPICIOUS = 1;
    public static final int NPC_BRAIN_STATUS_ALERT = 2;
    public static final int NPC_BRAIN_STATUS_SEARCHING = 3;
    public static final int NPC_BRAIN_STATUS_FOLLOWING = 4;
    public static final int NPC_BRAIN_STATUS_WANDERING = 5;

    public static final int NPC_BEHAVIOR_IDLE = 0;
    public static final int NPC_BEHAVIOR_MOVE = 1;
    public static final int NPC_BEHAVIOR_PATROL = 2;
    public static final int NPC_BEHAVIOR_FOLLOW = 3;
    public static final int STANCE_AGGRESSIVE = 1;
    public static final int STANCE_DEFENSIVE = 2;
    public static final int STANCE_STAND_GROUND = 3;
    public static final int STANCE_NO_ATTACK = 4;
    public static final BigDecimal NPC_ARRIVE_DISTANCE = BigDecimal.valueOf(0.1);
    public static final BigDecimal NPC_FOLLOW_STOP_DISTANCE = BigDecimal.ONE;
    public static final int NPC_ROLE_INDIVIDUAL = 1;
    public static final int NPC_ROLE_PEER = 2;
    public static final int NPC_ROLE_MINION = 3;
    public static final int NPC_EXEMPTION_ALL = 0;
    public static final int NPC_EXEMPTION_INNOCENT = 1;
    public static final int NPC_EXEMPTION_TEAMMATE = 2;
    public static final int NPC_EXEMPTION_SAME_CREATURE = 3;
    public static final int NPC_EXEMPTION_LENGTH = 4;

    public static final BigDecimal DEFAULT_DAYTIME_VISION_RADIUS = BigDecimal.valueOf(5);
    public static final BigDecimal DEFAULT_NIGHT_VISION_RADIUS = BigDecimal.valueOf(2);
    public static final BigDecimal DEFAULT_DISTINCT_VISION_ANGLE = BigDecimal.valueOf(120);
    public static final BigDecimal DEFAULT_INDISTINCT_VISION_ANGLE = BigDecimal.valueOf(180);
    public static final BigDecimal DEFAULT_DISTINCT_HEARING_RADIUS = BigDecimal.valueOf(2);
    public static final BigDecimal DEFAULT_INDISTINCT_HEARING_RADIUS = BigDecimal.valueOf(10);

    public static final String ORIGIN_CHINESE = "ORIGIN_CHINESE";
    public static final String ORIGIN_JAPANESE = "ORIGIN_JAPANESE";
    public static final String ORIGIN_INTERNATIONAL = "ORIGIN_INTERNATIONAL";
    public static final int AVATARS_LENGTH = 110;
    public static final int BREAST_TYPE_FEMALE_LENGTH = 10;
    public static final int ACCESSORY_TYPE_FEMALE_LENGTH = 10;
    public static final int EYES_LENGTH = 10;
    public static final int NOSE_LENGTH = 10;
    public static final int MOUTH_LENGTH = 10;
    public static final int TONGUE_LENGTH = 10;
    public static final int EYEBROWS_LENGTH = 10;
    public static final int MOUSTACHE_LENGTH = 10;
    public static final int BEARD_LENGTH = 6;

    public static final int CAPACITY_MAX = 50;
}
