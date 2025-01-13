package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.model.GamepalError;

public class ErrorUtil {
    private ErrorUtil() {}
    public static final String CODE_SUCCESS = "0200";
    public static final GamepalError ERROR_1001 = new GamepalError("1001", "Generating UUID code failed.");
    public static final GamepalError ERROR_1002 = new GamepalError("1002", "Validating and converting request failed.");
    public static final GamepalError ERROR_1003 = new GamepalError("1003", "Websocket connection has gone.");
    public static final GamepalError ERROR_1004 = new GamepalError("1004", "Username is already used.");
    public static final GamepalError ERROR_1005 = new GamepalError("1005", "Invalid username or password.");
    public static final GamepalError ERROR_1006 = new GamepalError("1006", "Invalid userCode or token.");
    public static final GamepalError ERROR_1007 = new GamepalError("1007", "Valid player information not found.");
    public static final GamepalError ERROR_1008 = new GamepalError("1008", "Invalid request format.");
    public static final GamepalError ERROR_1009 = new GamepalError("1009", "Message receiver is not online.");
    public static final GamepalError ERROR_1010 = new GamepalError("1010", "Communicating failed.");
    public static final GamepalError ERROR_1011 = new GamepalError("1011", "Contact does not exist.");
    public static final GamepalError ERROR_1012 = new GamepalError("1012", "Invalid block code.");
    public static final GamepalError ERROR_1013 = new GamepalError("1013", "Invalid block type.");
    public static final GamepalError ERROR_1014 = new GamepalError("1014", "Invalid farmStatus");
    public static final GamepalError ERROR_1015 = new GamepalError("1015", "World with this worldCode has been created.");
    public static final GamepalError ERROR_1016 = new GamepalError("1016", "Invalid worldCode.");
    public static final GamepalError ERROR_1017 = new GamepalError("1017", "Different worlds detected.");
    public static final GamepalError ERROR_1018 = new GamepalError("1018", "Already offline.");
    public static final GamepalError ERROR_1019 = new GamepalError("1019", "Expired world.");
    public static final GamepalError ERROR_1020 = new GamepalError("1020", "Invalid item information.");
    public static final GamepalError ERROR_1021 = new GamepalError("1021", "Unable to locate terminal.");
    public static final GamepalError ERROR_1022 = new GamepalError("1022", "Unable to locate created game.");
    public static final GamepalError ERROR_1023 = new GamepalError("1023", "Invalid recipe information.");
    public static final GamepalError ERROR_1024 = new GamepalError("1024", "Player does not fit the condition.");
    public static final GamepalError ERROR_1025 = new GamepalError("1025", "Game type is not supported.");
    public static final GamepalError ERROR_1026 = new GamepalError("1026", "Game status is not supported.");
    public static final GamepalError ERROR_1027 = new GamepalError("1027", "Invalid regionNo.");
    public static final GamepalError ERROR_1028 = new GamepalError("1028", "Invalid skillNo.");
    public static final GamepalError ERROR_1029 = new GamepalError("1029", "Invalid skillMode.");
    public static final GamepalError ERROR_1030 = new GamepalError("1030", "Invalid drop id.");
    public static final GamepalError ERROR_1031 = new GamepalError("1035", "Invalid coordinate.");
    public static final GamepalError ERROR_1032 = new GamepalError("1036", "Invalid JSON content.");
    public static final GamepalError ERROR_1033 = new GamepalError("1033", "Set membership failed.");
    public static final GamepalError ERROR_1034 = new GamepalError("1034", "Invalid interactionInfo.");
    public static final GamepalError ERROR_1035 = new GamepalError("1035", "Item amount is not enough.");
    public static final GamepalError ERROR_1036 = new GamepalError("1036", "Unable to kill invincible player.");
    public static final GamepalError ERROR_1037 = new GamepalError("1037", "Invalid creatureType.");
    public static final GamepalError ERROR_1038 = new GamepalError("1038", "Invalid animal skinColor.");
    public static final GamepalError ERROR_1039 = new GamepalError("1039", "Invalid playerType.");
    public static final GamepalError ERROR_1040 = new GamepalError("1040", "Not enough ammo.");
    public static final GamepalError ERROR_1041 = new GamepalError("1041", "Invalid sceneNo.");
    public static final GamepalError ERROR_1042 = new GamepalError("1042", "Invalid cropCode.");

    public static final GamepalError ERROR_1101 = new GamepalError("1101", "Invalid gameNo.");
    public static final GamepalError ERROR_1102 = new GamepalError("1102", "Invalid playerCount.");
    public static final GamepalError ERROR_1103 = new GamepalError("1103", "Invalid gameStatus.");
    public static final GamepalError ERROR_1104 = new GamepalError("1104", "Invalid playerStatus.");
}
