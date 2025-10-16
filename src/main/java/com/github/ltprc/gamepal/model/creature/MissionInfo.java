package com.github.ltprc.gamepal.model.creature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionInfo {
    private int status; // 0-initiated 1-completed 2-failed
    private String content;
}
