package com.github.ltprc.gamepal.config;

import java.math.BigDecimal;

public class RegionConstants {

    private RegionConstants() {}

    // Frontend constants

    // Backend constants

    public static final int SCENE_WIDTH_DEFAULT = 10;
    public static final int SCENE_HEIGHT_DEFAULT = 10;
    public static final int REGION_RADIUS_DEFAULT = 50;
    public static final BigDecimal SCENE_ALTITUDE_DEFAULT = BigDecimal.ZERO;
    public static final int REGION_TYPE_EMPTY = 0;
    public static final int REGION_TYPE_ISLAND = 10;
    public static final int REGION_TYPE_ALL_DIRT = BlockConstants.BLOCK_CODE_DIRT;
    public static final int REGION_TYPE_ALL_SAND = BlockConstants.BLOCK_CODE_SAND;
    public static final int REGION_TYPE_ALL_GRASS = BlockConstants.BLOCK_CODE_GRASS;
    public static final int REGION_TYPE_ALL_SNOW = BlockConstants.BLOCK_CODE_SNOW;
    public static final int REGION_TYPE_ALL_SWAMP = BlockConstants.BLOCK_CODE_SWAMP;
    public static final int REGION_TYPE_ALL_ROUGH = BlockConstants.BLOCK_CODE_ROUGH;
    public static final int REGION_TYPE_ALL_SUBTERRANEAN = BlockConstants.BLOCK_CODE_SUBTERRANEAN;
    public static final int REGION_TYPE_ALL_LAVA = BlockConstants.BLOCK_CODE_LAVA;
    public static final int REGION_TYPE_ALL_WATER_SHALLOW = BlockConstants.BLOCK_CODE_WATER_SHALLOW;
    public static final int REGION_TYPE_ALL_WATER_MEDIUM = BlockConstants.BLOCK_CODE_WATER_MEDIUM;
    public static final int REGION_TYPE_ALL_WATER_DEEP = BlockConstants.BLOCK_CODE_WATER_DEEP;
}
