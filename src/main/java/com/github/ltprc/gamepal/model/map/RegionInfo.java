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

    public RegionInfo(RegionInfo regionInfo) {
        regionNo = regionInfo.getRegionNo();
        name = regionInfo.getName();
        height = regionInfo.getHeight();
        width = regionInfo.getWidth();
    }
}
