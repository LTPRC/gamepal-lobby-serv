package com.github.ltprc.gamepal.config;

public class BuffConstants {

    // Frontend constants

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
    public static final int BUFF_CODE_DIVING = 21;
    public static final int BUFF_CODE_DROWNING = 22;
    public static final int BUFF_CODE_LENGTH = 23;

    // Backend constants

    public static final int BUFF_DEFAULT_FRAME_DEAD = 10 * GamePalConstants.FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_KNOCKED = 10 * GamePalConstants.FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_BLOCKED = 1 * GamePalConstants.FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_HAPPY = 10 * GamePalConstants.FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_SAD = 10 * GamePalConstants.FRAME_PER_SECOND;
    public static final int BUFF_DEFAULT_FRAME_DIVING = 10 * GamePalConstants.FRAME_PER_SECOND;

    private BuffConstants() {
    }
}
