package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.model.GamepalError;

public class ErrorUtil {
    public static final String CODE_SUCCESS = "0200";
    public static final GamepalError ERROR_1001 = new GamepalError("1001", "Generating UUID code failed.");
    public static final GamepalError ERROR_1002 = new GamepalError("1002", "Validating and converting request failed.");
    public static final GamepalError ERROR_1003 = new GamepalError("1003", "");
    public static final GamepalError ERROR_1004 = new GamepalError("1004", "Username is already taken.");
    public static final GamepalError ERROR_1005 = new GamepalError("1005", "Invalid username or password.");
    public static final GamepalError ERROR_1006 = new GamepalError("1006", "Invalid userCode or token.");
    public static final GamepalError ERROR_1007 = new GamepalError("1007", "Token not found.");
    public static final GamepalError ERROR_1008 = new GamepalError("1008", "Invalid request format.");
    public static final GamepalError ERROR_1009 = new GamepalError("1009", "Message receiver is not online.");
    public static final GamepalError ERROR_1010 = new GamepalError("1010", "Sending message failed.");
    public static final GamepalError ERROR_1011 = new GamepalError("1011", "Contact does not exist.");
    public static final GamepalError ERROR_1012 = new GamepalError("1012", "Invalid dropCode.");
    public static final GamepalError ERROR_1013 = new GamepalError("1013", "Amount exceeded.");

}
