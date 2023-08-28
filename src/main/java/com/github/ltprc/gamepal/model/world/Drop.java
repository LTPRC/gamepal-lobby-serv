package com.github.ltprc.gamepal.model.world;

import com.github.ltprc.gamepal.model.map.RegionCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drop extends RegionCoordinate {

    private String code;
    private String itemNo;
    private int amount;
}
