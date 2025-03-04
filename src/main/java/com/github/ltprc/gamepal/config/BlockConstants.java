package com.github.ltprc.gamepal.config;

import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BlockConstants {

    public static final int BLOCK_CODE_UPGRADE = 105;
    public static final int BLOCK_CODE_EXPLODE = 108;
    public static final int BLOCK_CODE_BLEED = 109;
    public static final int BLOCK_CODE_BLOCK = 110;
    public static final int BLOCK_CODE_HEAL = 111;
    public static final int BLOCK_CODE_DECAY = 112;
    public static final int BLOCK_CODE_SACRIFICE = 113;
    public static final int BLOCK_CODE_TAIL_SMOKE = 114;
    public static final int BLOCK_CODE_CHEER = 115;
    public static final int BLOCK_CODE_CURSE = 116;
    public static final int BLOCK_CODE_MELEE_HIT = 101;
    public static final int BLOCK_CODE_MELEE_KICK = 120;
    public static final int BLOCK_CODE_MELEE_SCRATCH = 117;
    public static final int BLOCK_CODE_MELEE_SMASH = 133;
    public static final int BLOCK_CODE_MELEE_CLEAVE = 118;
    public static final int BLOCK_CODE_MELEE_CHOP = 130;
    public static final int BLOCK_CODE_MELEE_PICK = 134;
    public static final int BLOCK_CODE_MELEE_STAB = 119;
    public static final int BLOCK_CODE_SHOOT_HIT = 122;
    public static final int BLOCK_CODE_SHOOT_ARROW = 123;
    public static final int BLOCK_CODE_SHOOT_SLUG = 107;
    public static final int BLOCK_CODE_SHOOT_MAGNUM = 124;
    public static final int BLOCK_CODE_SHOOT_ROCKET = 121;
    public static final int BLOCK_CODE_SHOOT_FIRE = 128;
    public static final int BLOCK_CODE_SHOOT_SPRAY = 129;
    public static final int BLOCK_CODE_SPARK = 125;
    public static final int BLOCK_CODE_NOISE = 126;
    public static final int BLOCK_CODE_MINE = 127;
    public static final int BLOCK_CODE_FIRE = 106;
    public static final int BLOCK_CODE_SPRAY = 131;
    public static final int BLOCK_CODE_SPARK_SHORT = 132;
    public static final int BLOCK_CODE_LIGHT_SMOKE = 135;
    public static final int BLOCK_CODE_BLEED_SEVERE = 136;
    public static final int BLOCK_CODE_DISINTEGRATE = 137;
    public static final int BLOCK_CODE_WAVE = 138;
    public static final int BLOCK_CODE_BUBBLE = 139;
    public static final int BLOCK_CODE_SHOCK = 140;
    public static final int BLOCK_CODE_NO_RESOURCE = 1000;
    public static final int BLOCK_CODE_BLACK = 1001;
    public static final int BLOCK_CODE_WHITE = 1002;
    public static final int BLOCK_CODE_TRANSPARENT = 1003;
    public static final int BLOCK_CODE_DIRT = 1010;
    public static final int BLOCK_CODE_SAND = 1011;
    public static final int BLOCK_CODE_GRASS = 1012;
    public static final int BLOCK_CODE_SNOW = 1013;
    public static final int BLOCK_CODE_SWAMP = 1014;
    public static final int BLOCK_CODE_ROUGH = 1015;
    public static final int BLOCK_CODE_SUBTERRANEAN = 1016;
    public static final int BLOCK_CODE_LAVA = 1017;
    public static final int BLOCK_CODE_WATER_SHALLOW = 1018;
    public static final int BLOCK_CODE_WATER_MEDIUM = 1019;
    public static final int BLOCK_CODE_WATER_DEEP = 1020;
    public static final int BLOCK_CODE_EDGE_SAND_UP = 1024;
    public static final int BLOCK_CODE_EDGE_SAND_LEFT = 1025;
    public static final int BLOCK_CODE_EDGE_SAND_RIGHT = 1026;
    public static final int BLOCK_CODE_EDGE_SAND_DOWN = 1027;
    public static final int BLOCK_CODE_EDGE_DIRT_UP = 1028;
    public static final int BLOCK_CODE_EDGE_DIRT_LEFT = 1029;
    public static final int BLOCK_CODE_EDGE_DIRT_RIGHT = 1030;
    public static final int BLOCK_CODE_EDGE_DIRT_DOWN = 1031;
    public static final int BLOCK_CODE_EDGE_WATER_SHALLOW_UP = 1032;
    public static final int BLOCK_CODE_EDGE_WATER_SHALLOW_LEFT = 1033;
    public static final int BLOCK_CODE_EDGE_WATER_SHALLOW_RIGHT = 1034;
    public static final int BLOCK_CODE_EDGE_WATER_SHALLOW_DOWN = 1035;
    public static final int BLOCK_CODE_EDGE_WATER_MEDIUM_UP = 1036;
    public static final int BLOCK_CODE_EDGE_WATER_MEDIUM_LEFT = 1037;
    public static final int BLOCK_CODE_EDGE_WATER_MEDIUM_RIGHT = 1038;
    public static final int BLOCK_CODE_EDGE_WATER_MEDIUM_DOWN = 1039;
    public static final int BLOCK_CODE_STONE_FLOOR_1 = 1040;
    public static final int BLOCK_CODE_STONE_FLOOR_2 = 1041;
    public static final int BLOCK_CODE_BITUMEN = 1042;
    public static final int BLOCK_CODE_WOODEN_FLOOR_1 = 1043;
    public static final int BLOCK_CODE_WOODEN_FLOOR_2 = 1044;
    public static final int BLOCK_CODE_BRICK_1 = 1100;
    public static final int BLOCK_CODE_BRICK_2 = 1101;
    public static final int BLOCK_CODE_BRICK_3 = 1104;
    public static final int BLOCK_CODE_BRICK_4 = 1105;
    public static final int BLOCK_CODE_BRICK_5 = 1112;
    public static final int BLOCK_CODE_BRICK_6 = 1113;
    public static final int BLOCK_CODE_BRICK_7 = 1114;
    public static final int BLOCK_CODE_BRICK_8 = 1115;
    public static final int BLOCK_CODE_LIME_WALL = 1116;
    public static final int BLOCK_CODE_HALF_LIME_WALL = 1123;
    public static final int BLOCK_CODE_DOOR_1 = 2100;
    public static final int BLOCK_CODE_DOOR_2 = 2101;
    public static final int BLOCK_CODE_DOOR_3 = 2102;
    public static final int BLOCK_CODE_DOOR_4 = 2103;
    public static final int BLOCK_CODE_DOOR_5 = 2104;
    public static final int BLOCK_CODE_DOOR_6 = 2105;
    public static final int BLOCK_CODE_DOOR_7 = 2106;
    public static final int BLOCK_CODE_DOOR_8 = 2107;
    public static final int BLOCK_CODE_DOOR_9 = 2108;
    public static final int BLOCK_CODE_DOOR_10 = 2109;
    public static final int BLOCK_CODE_DOOR_11 = 2110;
    public static final int BLOCK_CODE_DOOR_12 = 2111;
    public static final int BLOCK_CODE_DOOR_13 = 2112;
    public static final int BLOCK_CODE_DOOR_14 = 2113;
    public static final int BLOCK_CODE_DOOR_15 = 2114;
    public static final int BLOCK_CODE_DOOR_16 = 2115;
    public static final int BLOCK_CODE_UPSTAIRS_UP = 2200;
    public static final int BLOCK_CODE_UPSTAIRS_DOWN = 2201;
    public static final int BLOCK_CODE_UPSTAIRS_LEFT = 2202;
    public static final int BLOCK_CODE_UPSTAIRS_RIGHT = 2203;
    public static final int BLOCK_CODE_DOWNSTAIRS_UP = 2204;
    public static final int BLOCK_CODE_DOWNSTAIRS_DOWN = 2205;
    public static final int BLOCK_CODE_DOWNSTAIRS_LEFT = 2206;
    public static final int BLOCK_CODE_DOWNSTAIRS_RIGHT = 2207;
    public static final int BLOCK_CODE_WINDOW_1 = 2300;
    public static final int BLOCK_CODE_WINDOW_2 = 2301;
    public static final int BLOCK_CODE_WINDOW_3 = 2302;
    public static final int BLOCK_CODE_WINDOW_4 = 2303;
    public static final int BLOCK_CODE_WINDOW_5 = 2304;
    public static final int BLOCK_CODE_WINDOW_6 = 2305;
    public static final int BLOCK_CODE_CRACK_1 = 2400;
    public static final int BLOCK_CODE_CRACK_2 = 2401;
    public static final int BLOCK_CODE_CRACK_3 = 2402;
    public static final int BLOCK_CODE_CHEST_CLOSE = 3001;
    public static final int BLOCK_CODE_CHEST_OPEN = 3002;
    public static final int BLOCK_CODE_SIGN = 3003;
    public static final int BLOCK_CODE_COOKER = 3004;
    public static final int BLOCK_CODE_SINK = 3005;
    public static final int BLOCK_CODE_SINGLE_BED = 3006;
    public static final int BLOCK_CODE_DOUBLE_BED = 3007;
    public static final int BLOCK_CODE_TOILET = 3008;
    public static final int BLOCK_CODE_STOVE = 3009;
    public static final int BLOCK_CODE_DRESSER_1 = 3010;
    public static final int BLOCK_CODE_DRESSER_2 = 3011;
    public static final int BLOCK_CODE_BENCH = 3012;
    public static final int BLOCK_CODE_DESK_1 = 3013;
    public static final int BLOCK_CODE_DESK_2 = 3014;
    public static final int BLOCK_CODE_TABLE_1 = 3015;
    public static final int BLOCK_CODE_TABLE_2 = 3016;
    public static final int BLOCK_CODE_TABLE_3 = 3017;
    public static final int BLOCK_CODE_DOCUMENT = 3021;
    public static final int BLOCK_CODE_BOX = 3100;
    public static final int BLOCK_CODE_ASH_PILE = 3102;
    public static final int BLOCK_CODE_WIRE_NETTING = 3103;
    public static final int BLOCK_CODE_PLAYER_DEFAULT = 3104;
    public static final int BLOCK_CODE_DROP_DEFAULT = 3105;
    public static final int BLOCK_CODE_TELEPORT_DEFAULT = 3106;
    public static final int BLOCK_CODE_HUMAN_REMAIN_DEFAULT = 3107;
    public static final int BLOCK_CODE_ANIMAL_REMAIN_DEFAULT = 3108;
    public static final int BLOCK_CODE_WORKSHOP_EMPTY = 4000;
    public static final int BLOCK_CODE_WORKSHOP_CONSTRUCTION = 4001;
    public static final int BLOCK_CODE_WORKSHOP_TOOL = 4002;
    public static final int BLOCK_CODE_WORKSHOP_AMMO = 4003;
    public static final int BLOCK_CODE_WORKSHOP_OUTFIT = 4004;
    public static final int BLOCK_CODE_WORKSHOP_CHEM = 4005;
    public static final int BLOCK_CODE_WORKSHOP_RECYCLE = 4006;
    public static final int BLOCK_CODE_SPEAKER = 4010;
    public static final int BLOCK_CODE_FARM = 4100;
    public static final int BLOCK_CODE_CROP_1 = 4101;
    public static final int BLOCK_CODE_CROP_2 = 4102;
    public static final int BLOCK_CODE_CROP_3 = 4103;
    public static final int BLOCK_CODE_CROP_0 = 4104;
    public static final int BLOCK_CODE_ROCK_1 = 5100;
    public static final int BLOCK_CODE_ROCK_2 = 5101;
    public static final int BLOCK_CODE_RAFFLESIA = 5102;
    public static final int BLOCK_CODE_STUMP = 5103;
    public static final int BLOCK_CODE_MOSSY_STUMP = 5104;
    public static final int BLOCK_CODE_HOLLOW_TRUNK = 5105;
    public static final int BLOCK_CODE_FLOWER_BUSH = 5106;
    public static final int BLOCK_CODE_BUSH = 5107;
    public static final int BLOCK_CODE_SMALL_FLOWER_1 = 5108;
    public static final int BLOCK_CODE_SMALL_FLOWER_2 = 5109;
    public static final int BLOCK_CODE_SMALL_FLOWER_3 = 5110;
    public static final int BLOCK_CODE_BIG_FLOWER_1 = 5111;
    public static final int BLOCK_CODE_BIG_FLOWER_2 = 5112;
    public static final int BLOCK_CODE_BIG_FLOWER_3 = 5113;
    public static final int BLOCK_CODE_MUSHROOM_1 = 5114;
    public static final int BLOCK_CODE_MUSHROOM_2 = 5115;
    public static final int BLOCK_CODE_GRASS_1 = 5116;
    public static final int BLOCK_CODE_GRASS_2 = 5117;
    public static final int BLOCK_CODE_GRASS_3 = 5118;
    public static final int BLOCK_CODE_GRASS_4 = 5119;
    public static final int BLOCK_CODE_CACTUS_1 = 5120;
    public static final int BLOCK_CODE_CACTUS_2 = 5121;
    public static final int BLOCK_CODE_CACTUS_3 = 5122;
    public static final int BLOCK_CODE_BIG_PINE = 6100;
    public static final int BLOCK_CODE_BIG_OAK = 6101;
    public static final int BLOCK_CODE_BIG_WITHERED_TREE = 6102;
    public static final int BLOCK_CODE_PINE = 6103;
    public static final int BLOCK_CODE_OAK = 6104;
    public static final int BLOCK_CODE_WITHERED_TREE = 6105;
    public static final int BLOCK_CODE_PALM = 6106;

    public static final int BLOCK_TYPE_NORMAL = 0;
    public static final int BLOCK_TYPE_EFFECT = 1;
    public static final int BLOCK_TYPE_PLAYER = 2;
    public static final int BLOCK_TYPE_DROP = 3;
    public static final int BLOCK_TYPE_TELEPORT = 4;
    public static final int BLOCK_TYPE_BED = 5;
    public static final int BLOCK_TYPE_TOILET = 6;
    public static final int BLOCK_TYPE_DRESSER = 7;
    public static final int BLOCK_TYPE_GAME = 9;
    public static final int BLOCK_TYPE_STORAGE = 10;
    public static final int BLOCK_TYPE_COOKER = 11;
    public static final int BLOCK_TYPE_SINK = 12;
    public static final int BLOCK_TYPE_CONTAINER = 13;
    public static final int BLOCK_TYPE_SPEAKER = 14;
    public static final int BLOCK_TYPE_BUILDING = 15;
    public static final int BLOCK_TYPE_TREE = 16;
    public static final int BLOCK_TYPE_ROCK = 17;
    public static final int BLOCK_TYPE_FARM = 18;
    public static final int BLOCK_TYPE_WORKSHOP = 20;
    public static final int BLOCK_TYPE_WORKSHOP_TOOL = 21;
    public static final int BLOCK_TYPE_WORKSHOP_AMMO = 22;
    public static final int BLOCK_TYPE_WORKSHOP_OUTFIT = 23;
    public static final int BLOCK_TYPE_WORKSHOP_CHEM = 24;
    public static final int BLOCK_TYPE_WORKSHOP_RECYCLE = 25;
    public static final int BLOCK_TYPE_TRAP = 30;
    public static final int BLOCK_TYPE_FLOOR = 31;
    public static final int BLOCK_TYPE_FLOOR_DECORATION = 32;
    public static final int BLOCK_TYPE_WALL = 33;
    public static final int BLOCK_TYPE_WALL_DECORATION = 34;
    public static final int BLOCK_TYPE_CEILING = 35;
    public static final int BLOCK_TYPE_CEILING_DECORATION = 36;
    public static final int BLOCK_TYPE_PLASMA = 37;
    public static final int BLOCK_TYPE_HUMAN_REMAIN_CONTAINER = 38;
    public static final int BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER = 39;

    public static final int CROP_PERIOD = 250;
    public static final int CROP_STATUS_NONE = 0;
    public static final int CROP_STATUS_PLANTED = 1;
    public static final int CROP_STATUS_MATURE = 2;
    public static final int CROP_STATUS_GATHERED = 3;

    // Backend constants

    public static final int STRUCTURE_LAYER_GROUND = 10; // Infrastructure objects including floor block
    public static final int STRUCTURE_LAYER_GROUND_DECORATION = 15;
    public static final int STRUCTURE_LAYER_BOTTOM = 20; // Low-level objects including ash pile, bloodstain
    public static final int STRUCTURE_LAYER_BOTTOM_DECORATION = 25;
    public static final int STRUCTURE_LAYER_MIDDLE = 30; // Active objects including creature, building
    public static final int STRUCTURE_LAYER_MIDDLE_DECORATION = 35;
    public static final int STRUCTURE_LAYER_TOP = 40; // High-level objects including ceiling block
    public static final int STRUCTURE_LAYER_TOP_DECORATION = 45;
    public static final int STRUCTURE_LAYER_SKY = 50; // Untouchable top-level objects
    public static final int STRUCTURE_LAYER_SKY_DECORATION = 55;

    public static final int STRUCTURE_MATERIAL_NONE = 0; // Collide to none positively
    public static final int STRUCTURE_MATERIAL_ALL = 1; // Collide to all positively
    public static final int STRUCTURE_MATERIAL_SOLID = 2; // Collide to 1, 2, 3, 4, 20 positively
    public static final int STRUCTURE_MATERIAL_SOLID_FLESH = 3; // Collide to 1, 2, 3, 20, 21 positively
    public static final int STRUCTURE_MATERIAL_SOLID_NO_FLESH = 4; // Collide to 1, 2, 4, 20 positively
    public static final int STRUCTURE_MATERIAL_PARTICLE = 10; // Collide to 1, 2, 3, 4, 20 positively
    public static final int STRUCTURE_MATERIAL_PARTICLE_NO_FLESH = 11; // Collide to 1, 2, 4, 20 positively
    public static final int STRUCTURE_MATERIAL_TARGET = 20; // Collide to none positively
    public static final int STRUCTURE_MATERIAL_TARGET_FLESH = 21; // Collide to none positively

    public static final int STRUCTURE_SHAPE_TYPE_ROUND = 1;
    public static final int STRUCTURE_SHAPE_TYPE_SQUARE = 2;
    public static final int STRUCTURE_SHAPE_TYPE_RECTANGLE = 3;
    public static final BigDecimal Z_DEFAULT = BigDecimal.ONE;
    public static final BigDecimal PLAYER_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal EVENT_RADIUS = BigDecimal.valueOf(0.25D);
    public static final BigDecimal MIN_DROP_INTERACTION_DISTANCE = BigDecimal.valueOf(0.3D);
    public static final BigDecimal ROUND_SCENE_OBJECT_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal MINE_RADIUS = BigDecimal.ONE;
    public static final BigDecimal WIRE_NETTING_RADIUS = BigDecimal.valueOf(0.4D);
    public static final BigDecimal FIRE_RADIUS = BigDecimal.valueOf(0.5D);
    public static final BigDecimal DROP_THROW_RADIUS = BigDecimal.valueOf(0.5D);
    public static final BigDecimal REMAIN_CONTAINER_THROW_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal BUBBLE_THROW_RADIUS = BigDecimal.valueOf(0.25D);
    public static final BigDecimal BLEED_RADIUS_MAX = BigDecimal.valueOf(0.1D);

    public static final int HP_DEFAULT = 100;
    public static final BigDecimal HP_PULL_RATIO = BigDecimal.valueOf(0.1D);
    public static final BigDecimal HP_RESPAWN_RATIO = BigDecimal.ONE;

    public static final BigDecimal Z_SPEED_DEFAULT = BigDecimal.ZERO;
    public static final BigDecimal MAX_SPEED_DEFAULT = BigDecimal.valueOf(0.2D);
    public static final BigDecimal ACCELERATION_MAX_SPEED_RATIO = BigDecimal.valueOf(0.1D);
    public static final BigDecimal FACE_DIRECTION_DEFAULT = BigDecimal.ZERO;
    public static final int FLOOR_CODE_DEFAULT = BLOCK_CODE_BLACK;
    public static final int FRAME_DEFAULT = 0;
    public static final int PERIOD_STATIC_DEFAULT = -1;
    public static final int PERIOD_DYNAMIC_DEFAULT = 25;
    public static final int FRAME_MAX_INFINITE_DEFAULT = -1;

    public static Map<Integer, Integer> BLOCK_CODE_TYPE_MAP = new HashMap<>();
    public static Map<Integer, Color> BLOCK_CODE_COLOR_MAP = new HashMap<>();

    static {
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_UPGRADE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EXPLODE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BLEED, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BLOCK, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_HEAL, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DECAY, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SACRIFICE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TAIL_SMOKE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CHEER, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CURSE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_HIT, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_KICK, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_SCRATCH, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_SMASH, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_CLEAVE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_CHOP, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_PICK, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MELEE_STAB, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_HIT, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_ARROW, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_SLUG, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_MAGNUM, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_ROCKET, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_FIRE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOOT_SPRAY, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SPARK, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_NOISE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MINE, BLOCK_TYPE_TRAP);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_FIRE, BLOCK_TYPE_PLASMA);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SPRAY, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SPARK_SHORT, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_LIGHT_SMOKE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BLEED_SEVERE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DISINTEGRATE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WAVE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BUBBLE, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SHOCK, BLOCK_TYPE_EFFECT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_NO_RESOURCE, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BLACK, BLOCK_TYPE_CEILING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WHITE, BLOCK_TYPE_CEILING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TRANSPARENT, BLOCK_TYPE_CEILING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DIRT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SAND, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_GRASS, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SNOW, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SWAMP, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_ROUGH, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SUBTERRANEAN, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_LAVA, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WATER_SHALLOW, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WATER_MEDIUM, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WATER_DEEP, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_SAND_UP, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_SAND_LEFT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_SAND_RIGHT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_SAND_DOWN, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_DIRT_UP, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_DIRT_LEFT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_DIRT_RIGHT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_DIRT_DOWN, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_SHALLOW_UP, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_SHALLOW_LEFT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_SHALLOW_RIGHT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_SHALLOW_DOWN, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_MEDIUM_UP, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_MEDIUM_LEFT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_MEDIUM_RIGHT, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_EDGE_WATER_MEDIUM_DOWN, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WOODEN_FLOOR_1, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WOODEN_FLOOR_2, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_STONE_FLOOR_1, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_STONE_FLOOR_2, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BITUMEN, BLOCK_TYPE_FLOOR);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_1, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_2, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_3, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_4, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_5, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_6, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_7, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BRICK_8, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_LIME_WALL, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_HALF_LIME_WALL, BLOCK_TYPE_WALL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_1, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_2, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_3, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_4, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_5, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_6, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_7, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_8, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_9, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_10, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_11, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_12, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_13, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_14, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_15, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOOR_16, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_UPSTAIRS_UP, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_UPSTAIRS_DOWN, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_UPSTAIRS_LEFT, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_UPSTAIRS_RIGHT, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOWNSTAIRS_UP, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOWNSTAIRS_DOWN, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOWNSTAIRS_LEFT, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOWNSTAIRS_RIGHT, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WINDOW_1, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WINDOW_2, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WINDOW_3, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WINDOW_4, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WINDOW_5, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WINDOW_6, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CRACK_1, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CRACK_2, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CRACK_3, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CHEST_CLOSE, BLOCK_TYPE_CONTAINER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CHEST_OPEN, BLOCK_TYPE_STORAGE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SIGN, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_COOKER, BLOCK_TYPE_COOKER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SINK, BLOCK_TYPE_SINK);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SINGLE_BED, BLOCK_TYPE_BED);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOUBLE_BED, BLOCK_TYPE_BED);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TOILET, BLOCK_TYPE_TOILET);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_STOVE, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DRESSER_1, BLOCK_TYPE_DRESSER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DRESSER_2, BLOCK_TYPE_DRESSER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BENCH, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DESK_1, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DESK_2, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TABLE_1, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TABLE_2, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TABLE_3, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DOCUMENT, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BOX, BLOCK_TYPE_CONTAINER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_PLAYER_DEFAULT, BLOCK_TYPE_PLAYER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_DROP_DEFAULT, BLOCK_TYPE_DROP);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_TELEPORT_DEFAULT, BLOCK_TYPE_TELEPORT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_HUMAN_REMAIN_DEFAULT, BLOCK_TYPE_HUMAN_REMAIN_CONTAINER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_ANIMAL_REMAIN_DEFAULT, BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_ASH_PILE, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WIRE_NETTING, BLOCK_TYPE_TRAP);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_EMPTY, BLOCK_TYPE_BUILDING);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_CONSTRUCTION, BLOCK_TYPE_WORKSHOP);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_TOOL, BLOCK_TYPE_WORKSHOP_TOOL);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_AMMO, BLOCK_TYPE_WORKSHOP_AMMO);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_OUTFIT, BLOCK_TYPE_WORKSHOP_OUTFIT);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_CHEM, BLOCK_TYPE_WORKSHOP_CHEM);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WORKSHOP_RECYCLE, BLOCK_TYPE_WORKSHOP_RECYCLE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SPEAKER, BLOCK_TYPE_SPEAKER);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_FARM, BLOCK_TYPE_FARM);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CROP_1, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CROP_2, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CROP_3, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CROP_0, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_ROCK_1, BLOCK_TYPE_ROCK);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_ROCK_2, BLOCK_TYPE_ROCK);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_RAFFLESIA, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_STUMP, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MOSSY_STUMP, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_HOLLOW_TRUNK, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_FLOWER_BUSH, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BUSH, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SMALL_FLOWER_1, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SMALL_FLOWER_2, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_SMALL_FLOWER_3, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BIG_FLOWER_1, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BIG_FLOWER_2, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BIG_FLOWER_3, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MUSHROOM_1, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_MUSHROOM_2, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_GRASS_1, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_GRASS_2, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_GRASS_3, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_GRASS_4, BLOCK_TYPE_FLOOR_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CACTUS_1, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CACTUS_2, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_CACTUS_3, BLOCK_TYPE_WALL_DECORATION);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BIG_PINE, BLOCK_TYPE_TREE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BIG_OAK, BLOCK_TYPE_TREE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_BIG_WITHERED_TREE, BLOCK_TYPE_TREE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_PINE, BLOCK_TYPE_TREE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_OAK, BLOCK_TYPE_TREE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_WITHERED_TREE, BLOCK_TYPE_TREE);
        BLOCK_CODE_TYPE_MAP.put(BLOCK_CODE_PALM, BLOCK_TYPE_TREE);

        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_BLACK, new Color(0, 0, 0));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_DIRT, new Color(168, 168, 96));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SAND, new Color(255, 255, 128));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_GRASS, new Color(0, 196, 0));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SNOW, new Color(255, 255, 255));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SWAMP, new Color(0, 128, 64));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_ROUGH, new Color(96, 96, 0));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SUBTERRANEAN, new Color(128, 128, 128));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_LAVA, new Color(96, 16, 16));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_WATER_SHALLOW, new Color(64, 192, 255));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_WATER_MEDIUM, new Color(48, 240, 192));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_WATER_DEEP, new Color(32, 96, 128));
    }

    private BlockConstants() {
    }
}
