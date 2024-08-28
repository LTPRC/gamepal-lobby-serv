package com.github.ltprc.gamepal.config;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BlockCodeConstants {

    // Backend constants

    public static final int REGION_RADIUS_DEFAULT = 50;
    public static final int REGION_TYPE_DEFAULT = 0;
    public static final int REGION_TYPE_ISLAND = 1;

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

    public static final char BLOCK_CODE_PREFIX_PLANTS = 'p';
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

    public static final char BLOCK_CODE_PREFIX_ROCKS = 'r';
    public static final int ROCK_INDEX_1 = 29;
    public static final int ROCK_INDEX_2 = 30;

    public static Map<Integer, Color> BLOCK_CODE_COLOR_MAP = new HashMap<>();

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
    }

    private BlockCodeConstants() {
    }
}
