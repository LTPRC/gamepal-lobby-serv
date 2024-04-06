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

    public Event(Event event) {
        super(event);
        userCode = event.userCode;
        code = event.code;
        frame = event.frame;
        frameMax = event.frameMax;
        period = event.period;
    }

    public Event(String userCode, int code, int frame, int frameMax, int period, Coordinate coordinate) {
        super(coordinate);
        this.userCode = userCode;
        this.code = code;
        this.frame = frame;
        this.frameMax = frameMax;
        this.period = period;
    }
}
