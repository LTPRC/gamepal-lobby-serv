package com.github.ltprc.gamepal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String fromUserCode;
    private String toUserCode;
    private Integer type; // 0-broadcast 1-chat
    private String content;
}
