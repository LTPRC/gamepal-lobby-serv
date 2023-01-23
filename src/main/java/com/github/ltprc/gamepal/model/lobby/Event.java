package com.github.ltprc.gamepal.model.lobby;

import com.github.ltprc.gamepal.model.map.SceneCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event extends SceneCoordinate {

    static {
        detectionType = "event";
    }
    private int eventType;
}
