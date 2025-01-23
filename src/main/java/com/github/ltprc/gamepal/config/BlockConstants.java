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
    public static final int BLOCK_CODE_NO_RESOURCE = 1000;
    public static final int BLOCK_CODE_BLACK = 1001;
    public static final int BLOCK_CODE_WHITE = 1002;
    public static final int BLOCK_CODE_DIRT = 1010;
    public static final int BLOCK_CODE_SAND = 1011;
    public static final int BLOCK_CODE_GRASS = 1012;
    public static final int BLOCK_CODE_SNOW = 1013;
    public static final int BLOCK_CODE_SWAMP = 1014;
    public static final int BLOCK_CODE_ROUGH = 1015;
    public static final int BLOCK_CODE_SUBTERRANEAN = 1016;
    public static final int BLOCK_CODE_LAVA = 1017;
    public static final int BLOCK_CODE_WATER = 1018;
    public static final int BLOCK_CODE_EDGE_DIRT_UP = 1020;
    public static final int BLOCK_CODE_EDGE_DIRT_LEFT = 1021;
    public static final int BLOCK_CODE_EDGE_DIRT_RIGHT = 1022;
    public static final int BLOCK_CODE_EDGE_DIRT_DOWN = 1023;
    public static final int BLOCK_CODE_EDGE_SAND_UP = 1024;
    public static final int BLOCK_CODE_EDGE_SAND_LEFT = 1025;
    public static final int BLOCK_CODE_EDGE_SAND_RIGHT = 1026;
    public static final int BLOCK_CODE_EDGE_SAND_DOWN = 1027;
    public static final int BLOCK_CODE_WOODEN_FLOOR_1 = 1030;
    public static final int BLOCK_CODE_WOODEN_FLOOR_2 = 1031;
    public static final int BLOCK_CODE_STONE_FLOOR_1 = 1040;
    public static final int BLOCK_CODE_STONE_FLOOR_2 = 1041;
    public static final int BLOCK_CODE_BITUMEN = 1042;
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
    public static final int BLOCK_CODE_PACK = 3101;
    public static final int BLOCK_CODE_ASH = 3102;
    public static final int BLOCK_CODE_WIRE_NETTING = 3103;
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

    public static final int CROP_PERIOD = 250;
    public static final int CROP_STATUS_NONE = 0;
    public static final int CROP_STATUS_PLANTED = 1;
    public static final int CROP_STATUS_MATURE = 2;
    public static final int CROP_STATUS_GATHERED = 3;

    // Backend constants

    public static final int STRUCTURE_LAYER_GROUND = 10;
    public static final int STRUCTURE_LAYER_GROUND_DECORATION = 15;
    public static final int STRUCTURE_LAYER_BOTTOM = 20;
    public static final int STRUCTURE_LAYER_BOTTOM_DECORATION = 25;
    public static final int STRUCTURE_LAYER_MIDDLE = 30;
    public static final int STRUCTURE_LAYER_MIDDLE_DECORATION = 35;
    public static final int STRUCTURE_LAYER_TOP = 40;
    public static final int STRUCTURE_LAYER_TOP_DECORATION = 45;
    public static final int STRUCTURE_LAYER_SKY = 50;
    public static final int STRUCTURE_LAYER_SKY_DECORATION = 55;

    public static final int STRUCTURE_MATERIAL_NONE = 0; // Collide to none positively
    public static final int STRUCTURE_MATERIAL_ALL = 1; // Collide to all positively
    public static final int STRUCTURE_MATERIAL_SOLID = 2; // Collide to 1, 2, 3, 4 positively
    public static final int STRUCTURE_MATERIAL_SOLID_FLESH = 3; // Collide to 1, 2, 3 positively
    public static final int STRUCTURE_MATERIAL_SOLID_NO_FLESH = 4; // Collide to 1, 2, 4 positively
    public static final int STRUCTURE_MATERIAL_PARTICLE = 10; // Collide to 1, 2, 3, 4 positively
    public static final int STRUCTURE_MATERIAL_PARTICLE_NO_FLESH = 11; // Collide to 1, 2, 4 positively

    public static final int STRUCTURE_SHAPE_TYPE_ROUND = 1;
    public static final int STRUCTURE_SHAPE_TYPE_SQUARE = 2;
    public static final int STRUCTURE_SHAPE_TYPE_RECTANGLE = 3;
    public static final BigDecimal PLAYER_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal EVENT_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal MIN_DROP_INTERACTION_DISTANCE = BigDecimal.valueOf(0.2D);
    public static final BigDecimal BARRIER_RADIUS = BigDecimal.valueOf(0.25D);
    public static final BigDecimal ROUND_SCENE_OBJECT_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal WIRE_NETTING_RADIUS = BigDecimal.valueOf(0.4D);

    public static final int HP_DEFAULT = 100;

    public static final BigDecimal MAX_SPEED_DEFAULT = BigDecimal.valueOf(0.1);
    public static final BigDecimal ACCELERATION_DEFAULT = BigDecimal.valueOf(0.005);
    public static final BigDecimal FACE_DIRECTION_DEFAULT = BigDecimal.ZERO;
    public static final int FLOOR_CODE_DEFAULT = 0;
    public static final int FRAME_DEFAULT = 0;
    public static final int FRAME_MAX_DEFAULT = -1;
    public static final int PERIOD_DEFAULT = 1;

    public static Map<Integer, Color> BLOCK_CODE_COLOR_MAP = new HashMap<>();
    public static Map<Integer, Long> BLOCK_TYPE_TIMEOUT_MAP = new HashMap<>();

    static {
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_BLACK, new Color(0, 0, 0));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_DIRT, new Color(168, 168, 96));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SAND, new Color(255, 255, 128));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_GRASS, new Color(0, 196, 0));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SNOW, new Color(255, 255, 255));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SWAMP, new Color(0, 128, 64));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_ROUGH, new Color(96, 96, 0));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_SUBTERRANEAN, new Color(128, 128, 128));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_LAVA, new Color(96, 16, 16));
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_WATER, new Color(64, 192, 255));

        BLOCK_TYPE_TIMEOUT_MAP.put(BLOCK_TYPE_PLAYER, 300L);
        BLOCK_TYPE_TIMEOUT_MAP.put(BLOCK_TYPE_DROP, 60L);
        BLOCK_TYPE_TIMEOUT_MAP.put(BLOCK_TYPE_CONTAINER, 300L);
    }

    private BlockConstants() {
    }
}
