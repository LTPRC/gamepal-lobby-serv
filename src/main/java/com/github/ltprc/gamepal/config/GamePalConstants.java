package com.github.ltprc.gamepal.config;

import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;

import java.math.BigDecimal;

public class GamePalConstants {

    private GamePalConstants() {}

    // Frontend constants

    public static final int WEB_STAGE_START = 0;
    public static final int WEB_STAGE_INITIALIZING = 1;
    public static final int WEB_STAGE_INITIALIZED = 2;
    public static final int PLAYER_STATUS_INIT = 0;
    public static final int PLAYER_STATUS_RUNNING = 1;
    public static final int FRAME_PER_SECOND = 25;
    public static final int MINI_MAP_DEFAULT_SIZE = 100;

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

    public static final BigDecimal MAX_WIND_SPEED = BigDecimal.valueOf(0.1D);

    // Backend constants

    public static final int MAX_WORLD_TIME = 86400;
    public static final int UPDATED_WORLD_TIME_PER_SECOND = 60;
    public static final int WORLD_TIME_SUNRISE_BEGIN = 18000;
    public static final int WORLD_TIME_SUNRISE_END = 25200;
    public static final int WORLD_TIME_SUNSET_BEGIN = 61200;
    public static final int WORLD_TIME_SUNSET_END = 68400;

    public static final int REGION_RADIUS_DEFAULT = 50;
    public static final int SCENE_DEFAULT_WIDTH = 10;
    public static final int SCENE_DEFAULT_HEIGHT = 10;
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

    public static final BigDecimal DROP_THROW_RADIUS = BigDecimal.valueOf(0.5D);
    public static final BigDecimal REMAIN_CONTAINER_THROW_RADIUS = BigDecimal.valueOf(0.25D);
    public static final BigDecimal BUBBLE_THROW_RADIUS = BigDecimal.valueOf(0.25D);
    public static final BigDecimal BLEED_RADIUS_MAX = BigDecimal.valueOf(0.1D);

    public static final long PLAYER_LOGOFF_THRESHOLD_IN_SECOND = 300L;
    public static final int DROP_DISAPPEAR_THRESHOLD_IN_FRAME = 60 * FRAME_PER_SECOND;

    public static final WorldCoordinate DEFAULT_BIRTHPLACE = new WorldCoordinate(1,
            new IntegerCoordinate(0, 0), new Coordinate(new BigDecimal(5), new BigDecimal(5)));
}
