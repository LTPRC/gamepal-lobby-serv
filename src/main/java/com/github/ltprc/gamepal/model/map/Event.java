package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event extends Coordinate {
    private String userCode;
    private int code;
    private int frame;
    private int frameMax;
    private int period;
}
