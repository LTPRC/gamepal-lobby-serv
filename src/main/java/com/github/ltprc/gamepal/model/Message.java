package com.github.ltprc.gamepal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Integer type; // 0-printed 1-voice
    private Integer scope; // 0-global 1-individual
    private String fromUserCode;
    private String toUserCode;
    private String content;

    public Message(Message message) {
        this.type = message.getType();
        this.scope = message.getScope();
        this.fromUserCode = message.getFromUserCode();
        this.toUserCode = message.getToUserCode();
        this.content = message.getContent();
    }
}
