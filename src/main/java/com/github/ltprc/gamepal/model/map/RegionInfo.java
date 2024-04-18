package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionInfo {
    private int regionNo;
    private String name;
    private int height;
    private int width;
    private int radius;

    public RegionInfo(RegionInfo regionInfo) {
        regionNo = regionInfo.regionNo;
        name = regionInfo.name;
        height = regionInfo.height;
        width = regionInfo.width;
        radius = regionInfo.radius;
    }
}
