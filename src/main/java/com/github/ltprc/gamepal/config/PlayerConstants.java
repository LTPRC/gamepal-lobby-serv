package com.github.ltprc.gamepal.config;

public class PlayerConstants {

    private PlayerConstants() {}

    // Backend constants

    public static final int PLAYER_TYPE_HUMAN = 0;
    public static final int PLAYER_TYPE_AI = 1;
    public static final int PLAYER_TYPE_ANIMAL = 3;

    public static final int NPC_BRAIN_STATUS_UNAWARE = 0;
    public static final int NPC_BRAIN_STATUS_SUSPICIOUS = 1;
    public static final int NPC_BRAIN_STATUS_ALERT = 2;
    public static final int NPC_BRAIN_STATUS_SEARCHING = 3;
    public static final int NPC_BRAIN_STATUS_FOLLOWING = 4;
    public static final int NPC_BRAIN_STATUS_WANDERING = 5;
}
