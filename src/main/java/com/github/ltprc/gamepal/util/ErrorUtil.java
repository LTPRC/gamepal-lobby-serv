package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.model.GamepalError;

public class ErrorUtil {
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
    public static final GamepalError ERROR_1012 = new GamepalError("1012", "Invalid dropCode.");
    public static final GamepalError ERROR_1013 = new GamepalError("1013", "Duplicated dropCode.");
    public static final GamepalError ERROR_1014 = new GamepalError("1014", "Game status is invalid.");
    public static final GamepalError ERROR_1015 = new GamepalError("1015", "World with this worldCode has been created.");
    public static final GamepalError ERROR_1016 = new GamepalError("1016", "Invalid worldCode.");
    public static final GamepalError ERROR_1017 = new GamepalError("1017", "Unable to communicate between different worlds.");
    public static final GamepalError ERROR_1018 = new GamepalError("1018", "Already offline.");

}
