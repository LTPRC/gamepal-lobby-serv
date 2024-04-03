package com.github.ltprc.gamepal.config;

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

    public static final String COMMAND_PREFIX = "/";
    public static final int MESSAGE_TYPE_PRINTED = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;
    public static final int MESSAGE_TYPE_TERMINAL = 3;

    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_INDIVIDUAL = 1;
    public static final int SCOPE_SELF = 2;

    public static final int SCENE_DEFAULT_WIDTH = 10;
    public static final int SCENE_DEFAULT_HEIGHT = 10;
    public static final int BLOCK_TYPE_GROUND = 0;
    public static final int BLOCK_TYPE_WALL = 1;
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
    public static final int BLOCK_TYPE_CEILING = 13;
    public static final int BLOCK_TYPE_GROUND_DECORATION = 14;
    public static final int BLOCK_TYPE_WALL_DECORATION = 15;
    public static final int BLOCK_TYPE_CEILING_DECORATION = 16;
    public static final int BLOCK_TYPE_BLOCKED_GROUND = 17;
    public static final int BLOCK_TYPE_HOLLOW_WALL = 18;
    public static final int BLOCK_TYPE_BLOCKED_CEILING = 19;
    public static final int BLOCK_TYPE_TREE = 20;

    public static final BigDecimal PLAYER_RADIUS = BigDecimal.valueOf(0.1);
    public static final BigDecimal PLAYER_VIEW_RADIUS = BigDecimal.valueOf(10);

    public static final int TREE_TYPE_PINE = 1;

    public static final int INTERACTION_USE = 0;
    public static final int INTERACTION_EXCHANGE = 1;
    public static final int INTERACTION_SLEEP = 2;
    public static final int INTERACTION_DRINK = 3;
    public static final int INTERACTION_DECOMPOSE = 4;
    public static final int INTERACTION_TALK = 5;
    public static final int INTERACTION_ATTACK = 6;
    public static final int INTERACTION_FLIRT = 7;
    public static final int INTERACTION_SET = 8;
    public static final int INTERACTION_YIELD = 9;

    public static final char ITEM_CHARACTER_TOOL = 't';
    public static final char ITEM_CHARACTER_OUTFIT = 'a';
    public static final char ITEM_CHARACTER_CONSUMABLE = 'c';
    public static final char ITEM_CHARACTER_MATERIAL = 'm';
    public static final char ITEM_CHARACTER_JUNK = 'j';
    public static final char ITEM_CHARACTER_NOTE = 'n';
    public static final char ITEM_CHARACTER_RECORDING = 'r';

    public static final String FLAG_UPDATE_ITEMS = "updateItems";
    public static final String FLAG_UPDATE_PRESERVED_ITEMS = "updatePreservedItems";

    public static final int TERMINAL_TYPE_GAME = 1;
    public static final int GAME_TYPE_LAS_VEGAS = 1;

    public static final int EVENT_CODE_HIT = 101;
    public static final int EVENT_CODE_HIT_FIRE = 102;
    public static final int EVENT_CODE_HIT_ICE = 103;
    public static final int EVENT_CODE_HIT_ELECTRICITY = 104;
    public static final int EVENT_CODE_UPGRADE = 105;
    public static final int EVENT_CODE_FIRE = 106;
    public static final int EVENT_CODE_SHOOT = 107;
    public static final int EVENT_CODE_EXPLODE = 108;
    public static final int EVENT_CODE_BLEED = 109;
    public static final int EVENT_CODE_BLOCK = 110;
    public static final int EVENT_CODE_HEAL = 111;
    public static final int EVENT_CODE_DISTURB = 112;
    public static final int EVENT_CODE_SACRIFICE = 113;
    public static final int EVENT_CODE_TAIL_SMOKE = 114;
    public static final int EVENT_CODE_CHEER = 115;
    public static final int EVENT_CODE_CURSE = 116;

    public static final BigDecimal EVENT_MAX_DISTANCE_FIRE = BigDecimal.valueOf(0.5D);
    public static final BigDecimal EVENT_MAX_DISTANCE_HIT = BigDecimal.ONE;
    public static final BigDecimal EVENT_MAX_DISTANCE_SHOOT = BigDecimal.valueOf(10);
    public static final BigDecimal EVENT_MAX_ANGLE_HIT = BigDecimal.valueOf(120D);
    public static final BigDecimal EVENT_MAX_DISTANCE_EXPLODE = BigDecimal.valueOf(5);
    public static final int EVENT_DAMAGE_PER_FRAME_FIRE = 1;
    public static final int EVENT_DAMAGE_HIT = 10;
    public static final int EVENT_DAMAGE_SHOOT = 200;
    public static final int EVENT_DAMAGE_EXPLODE = 500;
    public static final int EVENT_HEAL_HEAL = 100;

    public static final int BUFF_CODE_DEAD = 1;
    public static final int BUFF_CODE_STUNNED = 2;
    public static final int BUFF_CODE_BLEEDING = 3;
    public static final int BUFF_CODE_SICK = 4;
    public static final int BUFF_CODE_FRACTURED = 5;
    public static final int BUFF_CODE_HUNGRY = 6;
    public static final int BUFF_CODE_THIRSTY = 7;
    public static final int BUFF_CODE_FATIGUED = 8;
    public static final int BUFF_CODE_BLIND = 9;
    public static final int BUFF_CODE_LENGTH = 10;

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


    public static final String ORIGIN_CHINESE = "ORIGIN_CHINESE";
    public static final String ORIGIN_JAPANESE = "ORIGIN_JAPANESE";
    public static final String ORIGIN_INTERNATIONAL = "ORIGIN_INTERNATIONAL";
    public static final String GENDER_MALE = "1";
    public static final String GENDER_FEMALE = "2";
    public static final int AVATARS_LENGTH = 10;
    public static final int SKIN_COLOR_C = 1;
    public static final int SKIN_COLOR_M = 2;
    public static final int SKIN_COLOR_A = 3;
    public static final int SKIN_COLOR_L = 4;
    public static final int SKIN_COLOR_B = 5;
    public static final int HAIRSTYLE_LENGTH = 5;
    public static final int EYES_LENGTH = 5;
    public static final int FACE_COEFS_LENGTH = 9;

    // Backend constants

    public static final int REGION_INDEX_NOTHING = 1;
    public static final int REGION_INDEX_GRASSLAND = 2;
    public static final int SCENE_SCAN_RADIUS = 1;
    public static final int SCENE_SCAN_MAX_RADIUS = 50;

    public static final long ONLINE_TIMEOUT_SECOND = 300;
    public static final int LAYER_BOTTOM = 1;
    public static final int LAYER_CENTER = 2;
    public static final int LAYER_TOP = 3;

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

    public static final int BUFF_DEFAULT_FRAME_DEAD = 10 * FRAME_PER_SECOND;
    public static final int SKILL_DEFAULT_TIME = 25;

    public static final int PLAYER_TYPE_HUMAN = 0;
    public static final int PLAYER_TYPE_AI = 1;
    public static final int NPC_TASK_TYPE_IDLE = 0;
    public static final int NPC_TASK_TYPE_OBSERVE = 1;
    public static final int NPC_TASK_TYPE_MOVE = 2;
    public static final int NPC_TASK_TYPE_ATTACK = 3;
    public static final BigDecimal NPC_MAX_OBSERVE_RANGE = BigDecimal.valueOf(5);
    public static final BigDecimal NPC_MAX_CHASE_DISTANCE = BigDecimal.valueOf(1);
}
