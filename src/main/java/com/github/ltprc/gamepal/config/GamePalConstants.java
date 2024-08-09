package com.github.ltprc.gamepal.config;

import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;

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

    public static final String COMMAND_PREFIX = "/";
    public static final int MESSAGE_TYPE_PRINTED = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;
    public static final int MESSAGE_TYPE_TERMINAL = 3;
    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_INDIVIDUAL = 1;
    public static final int SCOPE_SELF = 2;

    public static final int SCENE_DEFAULT_WIDTH = 10;
    public static final int SCENE_DEFAULT_HEIGHT = 10;
    public static final int BLOCK_TYPE_NORMAL = 0;
    public static final int BLOCK_TYPE_EVENT = 1;
    public static final int BLOCK_TYPE_PLAYER = 2;
    public static final int BLOCK_TYPE_DROP = 3;
    public static final int BLOCK_TYPE_TELEPORT = 4;
    public static final int BLOCK_TYPE_BED = 5;
    public static final int BLOCK_TYPE_TOILET = 6;
    public static final int BLOCK_TYPE_DRESSER = 7;
    public static final int BLOCK_TYPE_WORKSHOP = 8;
    public static final int BLOCK_TYPE_GAME = 9;
    public static final int BLOCK_TYPE_STORAGE = 10;
    public static final int BLOCK_TYPE_COOKER = 11;
    public static final int BLOCK_TYPE_SINK = 12;
    public static final int BLOCK_TYPE_CONTAINER = 13;

    public static final int STRUCTURE_MATERIAL_HOLLOW = 0;
    public static final int STRUCTURE_MATERIAL_SOLID = 1;
    public static final int STRUCTURE_MATERIAL_FLESH = 2;
    public static final int STRUCTURE_SHAPE_TYPE_ROUND = 1;
    public static final int STRUCTURE_SHAPE_TYPE_SQUARE = 2;
    public static final int STRUCTURE_SHAPE_TYPE_RECTANGLE = 3;
    public static final BigDecimal PLAYER_RADIUS = BigDecimal.valueOf(0.1);
    public static final BigDecimal MIN_DROP_INTERACTION_DISTANCE = BigDecimal.valueOf(0.2);

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

    public static final char ITEM_CHARACTER_TOOL = 't';
    public static final char ITEM_CHARACTER_OUTFIT = 'a';
    public static final char ITEM_CHARACTER_CONSUMABLE = 'c';
    public static final char ITEM_CHARACTER_MATERIAL = 'm';
    public static final char ITEM_CHARACTER_JUNK = 'j';
    public static final char ITEM_CHARACTER_NOTE = 'n';
    public static final char ITEM_CHARACTER_RECORDING = 'r';
    public static final char RECIPE_CHARACTER_WORKSHOP = 'w';
    public static final char RECIPE_CHARACTER_COOKER = 'c';
    public static final char RECIPE_CHARACTER_SINK = 's';

    public static final int TERMINAL_TYPE_GAME = 1;
    public static final int GAME_TYPE_LAS_VEGAS = 1;

    public static final int EVENT_CODE_HIT_FIRE = 102;
    public static final int EVENT_CODE_HIT_ICE = 103;
    public static final int EVENT_CODE_HIT_ELECTRICITY = 104;
    public static final int EVENT_CODE_UPGRADE = 105;
    public static final int EVENT_CODE_FIRE = 106;
    public static final int EVENT_CODE_EXPLODE = 108;
    public static final int EVENT_CODE_BLEED = 109;
    public static final int EVENT_CODE_BLOCK = 110;
    public static final int EVENT_CODE_HEAL = 111;
    public static final int EVENT_CODE_DISTURB = 112;
    public static final int EVENT_CODE_SACRIFICE = 113;
    public static final int EVENT_CODE_TAIL_SMOKE = 114;
    public static final int EVENT_CODE_CHEER = 115;
    public static final int EVENT_CODE_CURSE = 116;
    public static final int EVENT_CODE_MELEE_HIT = 101;
    public static final int EVENT_CODE_MELEE_SCRATCH = 117;
    public static final int EVENT_CODE_MELEE_CLEAVE = 118;
    public static final int EVENT_CODE_MELEE_STAB = 119;
    public static final int EVENT_CODE_MELEE_KICK = 120;
    public static final int EVENT_CODE_SHOOT_HIT = 122;
    public static final int EVENT_CODE_SHOOT_ARROW = 123;
    public static final int EVENT_CODE_SHOOT_SLUG = 107;
    public static final int EVENT_CODE_SHOOT_MAGNUM = 124;
    public static final int EVENT_CODE_SHOOT_ROCKET = 121;
    public static final int EVENT_CODE_SPARK = 125;
    public static final int EVENT_CODE_FOOTSTEP = 126;

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
    public static final int BUFF_CODE_REVIVED = 11;
    public static final int BUFF_CODE_REALISTIC = 12;
    public static final int BUFF_CODE_ANTI_TROPHY = 13;
    public static final int BUFF_CODE_BLOCKED = 14;
    public static final int BUFF_CODE_HAPPY = 15;
    public static final int BUFF_CODE_SAD = 16;
    public static final int BUFF_CODE_RECOVERING = 17;
    public static final int BUFF_CODE_LENGTH = 18;

    public static final int FACE_COEFS_LENGTH = 10;

    // Backend constants

    public static final long ONLINE_TIMEOUT_SECOND = 300L;
    public static final int SCENE_SCAN_RADIUS = 2;
    public static final int SCENE_SCAN_MAX_RADIUS = 50;

    public static final int GAME_STATUS_END = -1;
    public static final int GAME_STATUS_START = 0;
    public static final int GAME_STATUS_WAITING = 1;
    public static final int GAME_STATUS_RUNNING = 2;
    public static final int GAME_PLAYER_STATUS_END = -1;
    public static final int GAME_PLAYER_STATUS_START = 0;
    public static final int GAME_PLAYER_STATUS_SEEKING = 1;
    public static final int GAME_PLAYER_STATUS_STANDBY = 2;
    public static final int GAME_PLAYER_STATUS_PREPARED = 3;
    public static final int GAME_PLAYER_STATUS_PLAYING = 4;

    public static final int TOOL_INDEX_DEFAULT = 0;
    public static final int TOOL_INDEX_PRIMARY = 1;
    public static final int TOOL_INDEX_SECONDARY = 2;

    public static final int OUTFIT_INDEX_DEFAULT = 0;
    public static final int OUTFIT_INDEX_CLOTHES = 1;

    public static final BigDecimal DROP_THROW_RADIUS = BigDecimal.ONE;
    public static final BigDecimal REMAIN_CONTAINER_THROW_RADIUS = BigDecimal.valueOf(0.25D);

    public static final int BUFF_DEFAULT_FRAME_DEAD = 10 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_BLOCKED = 1 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_HAPPY = 10 * FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_SAD = 10 * FRAME_PER_SECOND;

    public static final BigDecimal EVENT_MAX_DISTANCE_FIRE = BigDecimal.valueOf(0.5D);
    public static final BigDecimal EVENT_MAX_DISTANCE_MELEE = BigDecimal.ONE;
    public static final BigDecimal EVENT_MAX_DISTANCE_SHOOT = BigDecimal.valueOf(10);
    public static final BigDecimal EVENT_MAX_DISTANCE_SHOOT_SHOTGUN = BigDecimal.valueOf(5);
    public static final BigDecimal EVENT_MAX_ANGLE_MELEE = BigDecimal.valueOf(120D);
    public static final BigDecimal EVENT_MAX_ANGLE_SHOOT = BigDecimal.valueOf(5D);
    public static final BigDecimal EVENT_MAX_ANGLE_SHOOT_SHOTGUN = BigDecimal.valueOf(10D);
    public static final BigDecimal EVENT_MAX_DISTANCE_EXPLODE = BigDecimal.valueOf(3);
    public static final int EVENT_DAMAGE_PER_FRAME_FIRE = 1;
    public static final int EVENT_DAMAGE_MELEE = 10;
    public static final int EVENT_DAMAGE_SHOOT = 200;
    public static final int EVENT_DAMAGE_EXPLODE = 500;
    public static final int EVENT_HEAL_HEAL = 100;

    public static final String ORIGIN_CHINESE = "ORIGIN_CHINESE";
    public static final String ORIGIN_JAPANESE = "ORIGIN_JAPANESE";
    public static final String ORIGIN_INTERNATIONAL = "ORIGIN_INTERNATIONAL";
    public static final int AVATARS_LENGTH = 110;
    public static final int SKIN_COLOR_C = 1;
    public static final int SKIN_COLOR_M = 2;
    public static final int SKIN_COLOR_A = 3;
    public static final int SKIN_COLOR_L = 4;
    public static final int SKIN_COLOR_B = 5;
    public static final int HAIRSTYLE_LENGTH = 5;
    public static final int EYES_LENGTH = 5;

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

    public static final int CAPACITY_MAX = 50;
    public static final WorldCoordinate DEFAULT_BIRTHPLACE = new WorldCoordinate(1,
            new IntegerCoordinate(0, 0), new Coordinate(new BigDecimal(5), new BigDecimal(5)));
}
