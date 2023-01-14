package com.github.ltprc.gamepal.model;

import lombok.Data;

@Data
public abstract class Message {
    protected String fromUserCode;
    protected String toUserCode;
    protected Integer type;
    protected String content;
}
