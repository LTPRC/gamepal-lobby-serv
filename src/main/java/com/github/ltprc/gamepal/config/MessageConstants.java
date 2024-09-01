package com.github.ltprc.gamepal.config;

public class MessageConstants {

    private MessageConstants() {}

    public static final int MESSAGE_TYPE_PRINTED = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;
    public static final int SCOPE_GLOBAL = 0;
    public static final int SCOPE_TEAMMATE = 1;
    public static final int SCOPE_INDIVIDUAL = 2;
    public static final int SCOPE_SELF = 3;

    // Backend constants

    public static final String COMMAND_PREFIX = "/";
    public static final String MESSAGE_PRINTED_CONTENT_VOICE = "[Voice]";
}
