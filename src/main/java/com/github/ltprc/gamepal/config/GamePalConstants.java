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
    public static final int MAX_WORLD_TIME = 86400;
    public static final int UPDATED_WORLD_TIME_PER_SECOND = 60;
    public static final int WORLD_TIME_SUNRISE_BEGIN = 18000;
    public static final int WORLD_TIME_SUNRISE_END = 25200;
    public static final int WORLD_TIME_SUNSET_BEGIN = 61200;
    public static final int WORLD_TIME_SUNSET_END = 68400;

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

    public static final int BUFF_CODE_DEAD = 1;
    public static final int BUFF_CODE_STUNNED = 2;
    public static final int BUFF_CODE_BLEEDING = 3;
    public static final int BUFF_CODE_SICK = 4;
    public static final int BUFF_CODE_FRACTURED = 5;
    public static final int BUFF_CODE_HUNGRY = 6;
    public static final int BUFF_CODE_THIRSTY = 7;
    public static final int BUFF_CODE_FATIGUED = 8;
    public static final int BUFF_CODE_BLIND = 9;
    public static final int BUFF_CODE_INVINCIBLE = 10;
    public static final int BUFF_CODE_ONE_HIT = 11;
    public static final int BUFF_CODE_REALISTIC = 12;
    public static final int BUFF_CODE_TROPHY = 13;
    public static final int BUFF_CODE_BLOCKED = 14;
    public static final int BUFF_CODE_HAPPY = 15;
    public static final int BUFF_CODE_SAD = 16;
    public static final int BUFF_CODE_RECOVERING = 17;
    public static final int BUFF_CODE_OVERWEIGHTED = 18;
    public static final int BUFF_CODE_KNOCKED = 19;
    public static final int BUFF_CODE_REVIVED = 20;
    public static final int BUFF_CODE_LENGTH = 21;

    // Backend constants

    public static final BigDecimal MAX_WIND_SPEED = BigDecimal.valueOf(0.1D);
    public static final int REGION_RADIUS_DEFAULT = 50;
    public static final int SCENE_DEFAULT_WIDTH = 10;
    public static final int SCENE_DEFAULT_HEIGHT = 10;
    public static final int REGION_TYPE_DEFAULT = 0;
    public static final int REGION_TYPE_ISLAND = 10;

    public static final BigDecimal DROP_THROW_RADIUS = BigDecimal.valueOf(0.5D);
    public static final BigDecimal REMAIN_CONTAINER_THROW_RADIUS = BigDecimal.valueOf(0.25D);

    public static final int BUFF_DEFAULT_FRAME_DEAD = 10 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_KNOCKED = 10 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_BLOCKED = 1 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_HAPPY = 10 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_SAD = 10 * FRAME_PER_SECOND;

    public static final long PLAYER_LOGOFF_THRESHOLD_IN_SECOND = 300L;
    public static final int DROP_DISAPPEAR_THRESHOLD_IN_FRAME = 60 * FRAME_PER_SECOND;

    public static final WorldCoordinate DEFAULT_BIRTHPLACE = new WorldCoordinate(1,
            new IntegerCoordinate(0, 0), new Coordinate(new BigDecimal(5), new BigDecimal(5)));
}
