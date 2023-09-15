package com.github.ltprc.gamepal.model.terminal;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface Terminal {
    String getId();
    String getUserCode();
    void addOutput(String output);
    List<String> flushOutput();
    JSONObject returnObject();
    void setStatus(int status);
}
