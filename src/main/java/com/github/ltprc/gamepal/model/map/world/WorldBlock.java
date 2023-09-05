package com.github.ltprc.gamepal.model.map.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldBlock extends WorldCoordinate {
    private Integer type;
    private String id;
    private String code;
}
