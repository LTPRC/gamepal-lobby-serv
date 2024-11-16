package com.github.ltprc.gamepal.config;

import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BlockConstants {

    public static final int BLOCK_CODE_NOTHING = 1001;
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

    public static final char BLOCK_CODE_PREFIX_PLANTS = 'p';
    public static final char BLOCK_CODE_PREFIX_ROCKS = 'r';

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
    public static final int BLOCK_TYPE_RADIO = 14;
    public static final int BLOCK_TYPE_BUILDING = 15;
    public static final int BLOCK_TYPE_TREE = 16;
    public static final int BLOCK_TYPE_ROCK = 17;
    public static final int BLOCK_TYPE_WORKSHOP = 20;
    public static final int BLOCK_TYPE_WORKSHOP_TOOL = 21;
    public static final int BLOCK_TYPE_WORKSHOP_AMMO = 22;
    public static final int BLOCK_TYPE_WORKSHOP_OUTFIT = 23;
    public static final int BLOCK_TYPE_WORKSHOP_CHEM = 24;
    public static final int BLOCK_TYPE_WORKSHOP_RECYCLE = 25;
    public static final int BLOCK_TYPE_TRAP = 30;

    // Backend constants

    public static final int PLANT_INDEX_BIG_PINE = 1;
    public static final int PLANT_INDEX_BIG_OAK = 2;
    public static final int PLANT_INDEX_BIG_WITHERED_TREE = 3;
    public static final int PLANT_INDEX_PINE = 4;
    public static final int PLANT_INDEX_OAK = 5;
    public static final int PLANT_INDEX_WITHERED_TREE = 6;
    public static final int PLANT_INDEX_PALM = 7;
    public static final int PLANT_INDEX_RAFFLESIA = 8;
    public static final int PLANT_INDEX_STUMP = 9;
    public static final int PLANT_INDEX_MOSSY_STUMP = 10;
    public static final int PLANT_INDEX_HOLLOW_TRUNK = 11;
    public static final int PLANT_INDEX_FLOWER_BUSH = 12;
    public static final int PLANT_INDEX_BUSH = 13;
    public static final int PLANT_INDEX_SMALL_FLOWER_1 = 14;
    public static final int PLANT_INDEX_SMALL_FLOWER_2 = 15;
    public static final int PLANT_INDEX_SMALL_FLOWER_3 = 16;
    public static final int PLANT_INDEX_BIG_FLOWER_1 = 17;
    public static final int PLANT_INDEX_BIG_FLOWER_2 = 18;
    public static final int PLANT_INDEX_BIG_FLOWER_3 = 19;
    public static final int PLANT_INDEX_MUSHROOM_1 = 20;
    public static final int PLANT_INDEX_MUSHROOM_2 = 21;
    public static final int PLANT_INDEX_GRASS_1 = 22;
    public static final int PLANT_INDEX_GRASS_2 = 23;
    public static final int PLANT_INDEX_GRASS_3 = 24;
    public static final int PLANT_INDEX_GRASS_4 = 25;
    public static final int PLANT_INDEX_CACTUS_1 = 26;
    public static final int PLANT_INDEX_CACTUS_2 = 27;
    public static final int PLANT_INDEX_CACTUS_3 = 28;
    public static final int ROCK_INDEX_1 = 29;
    public static final int ROCK_INDEX_2 = 30;

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

    public static final int STRUCTURE_MATERIAL_HOLLOW = 0; // Collide to none
    public static final int STRUCTURE_MATERIAL_SOLID = 1; // Collide to 1, 2, 3, 4
    public static final int STRUCTURE_MATERIAL_FLESH = 2; // Collide to 1, 2
    public static final int STRUCTURE_MATERIAL_MAGNUM = 3; // Collide to 1, 3
    public static final int STRUCTURE_MATERIAL_PLASMA = 4; // Collide to 1
    public static final int STRUCTURE_SHAPE_TYPE_ROUND = 1;
    public static final int STRUCTURE_SHAPE_TYPE_SQUARE = 2;
    public static final int STRUCTURE_SHAPE_TYPE_RECTANGLE = 3;
    public static final BigDecimal PLAYER_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal EVENT_RADIUS = BigDecimal.valueOf(0.1D);
    public static final BigDecimal MIN_DROP_INTERACTION_DISTANCE = BigDecimal.valueOf(0.2D);
    public static final BigDecimal BARRIER_RADIUS = BigDecimal.valueOf(0.25D);
    public static final BigDecimal ROUND_SCENE_OBJECT_RADIUS = BigDecimal.valueOf(0.1D);

    public static final int HP_DEFAULT = 10000;

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
        BLOCK_CODE_COLOR_MAP.put(BLOCK_CODE_NOTHING, new Color(0, 0, 0));
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
