package com.github.ltprc.gamepal.config;

public class GamePalConstants {

    // Frontend constants

    public static final int WEB_STAGE_START = 0;
    public static final int WEB_STAGE_INITIALIZING = 1;
    public static final int WEB_STAGE_INITIALIZED = 2;
    public static final int PLAYER_STATUS_INIT = 0;
    public static final int PLAYER_STATUS_RUNNING = 1;

    public static final int MESSAGE_TYPE_PRINTED = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;
    public static final int MESSAGE_TYPE_TERMINAL = 3;

    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_INDIVIDUAL = 1;
    public static final int SCOPE_SELF = 2;

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

    public static final int INTERACTION_USE = 0;
    public static final int INTERACTION_EXCHANGE = 1;
    public static final int INTERACTION_SLEEP = 2;
    public static final int INTERACTION_DRINK = 3;
    public static final int INTERACTION_DECOMPOSE = 4;
    public static final int INTERACTION_TALK = 5;
    public static final int INTERACTION_ATTACK = 6;
    public static final int INTERACTION_FLIRT = 7;
    public static final int INTERACTION_SET = 8;

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

    public static final int EVENT_CODE_BLEED = 101;
    public static final int EVENT_CODE_EXPLODE = 102;
    public static final int EVENT_CODE_HIT = 103;
    public static final int EVENT_CODE_HEAL = 104;

    // Backend constants

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
}
