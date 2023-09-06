package com.github.ltprc.gamepal.config;

public class GamePalConstants {

    // Frontend constants

    public static final int PLAYER_STATUS_INIT = 0;
    public static final int PLAYER_STATUS_RUNNING = 1;

    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_INDIVIDUAL = 1;

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

    // Backend constants

    public static final long ONLINE_TIMEOUT_SECOND = 300;
    public static final int MESSAGE_TYPE_PRINTED = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;
    public static final int LAYER_BOTTOM = 1;
    public static final int LAYER_CENTER = 2;
    public static final int LAYER_TOP = 3;
}
