package com.github.ltprc.gamepal.config;

public class MessageConstants {

    private MessageConstants() {}

    public static final int MESSAGE_TYPE_PRINTED = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;
    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_TEAMMATE = 1;
    public static final int SCOPE_INDIVIDUAL = 2;
    public static final int SCOPE_SELF = 3;
    public static final int SCOPE_NEARBY = 4;

    // Backend constants

    public static final String COMMAND_PREFIX = "/";
    public static final String MESSAGE_PRINTED_CONTENT_VOICE = "[Voice]";

    public static final String TRANSACTION_TYPE_UPDATE_HP = "UPDATE_HP";
    public static final String TRANSACTION_TYPE_UPDATE_VP = "UPDATE_VP";
    public static final String TRANSACTION_TYPE_UPDATE_HUNGER = "UPDATE_HUNGER";
    public static final String TRANSACTION_TYPE_UPDATE_THIRST = "UPDATE_THIRST";

    public static final int CHAT_DISPLAY_LINE_CHAR_SIZE_MAX = 50;
}
