package com.github.ltprc.gamepal.model.map.block;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventInfo {

    private String eventId;
    private int frame;
    private int frameMax;
    private int period;

    public EventInfo(EventInfo eventInfo) {
        if (null == eventInfo) {
            return;
        }
        eventId = eventInfo.eventId;
        frame = eventInfo.frame;
        frameMax = eventInfo.frameMax;
        period = eventInfo.period;
    }

    public EventInfo(String eventId, int frame, int frameMax, int period) {
        this.eventId = eventId;
        this.frame = frame;
        this.frameMax = frameMax;
        this.period = period;
    }
}
