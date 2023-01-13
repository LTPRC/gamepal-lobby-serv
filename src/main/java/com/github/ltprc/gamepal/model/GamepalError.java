package com.github.ltprc.gamepal.model;

public class GamepalError {

    private String code;
    private String msg;

    public GamepalError() {

    }

    public GamepalError(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
