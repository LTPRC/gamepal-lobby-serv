package com.github.ltprc.gamepal.terminal;

import java.util.List;

public interface Terminal {
    int getType();
    String getId();
    String getUserCode();
    void addOutput(String output);
    List<String> flushOutput();
    void setStatus(int status);
}
