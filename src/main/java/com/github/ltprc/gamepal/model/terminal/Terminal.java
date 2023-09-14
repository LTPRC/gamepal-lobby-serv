package com.github.ltprc.gamepal.model.terminal;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface Terminal {
    void checkStatus();
    void input(String input);
    String getId();
    String getUserCode();
    List<String> flushOutput();
    JSONObject returnObject();
}
