package com.github.ltprc.gamepal.config;

public class GameConstants {

    private GameConstants() {}

    public static final int TERMINAL_TYPE_GAME = 1;
    public static final int GAME_TYPE_LAS_VEGAS = 1;
    public static final int GAME_TYPE_PUBG = 2;

    // Backend constants

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

    public static final int PUBG_PLAYER_COUNT_MIN = 2;
    public static final int PUBG_PLAYER_COUNT_MAX = 16;
}
