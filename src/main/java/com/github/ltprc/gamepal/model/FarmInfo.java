package com.github.ltprc.gamepal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmInfo {
    private String cropCode;
    private int cropStatus; // -1 no crop 0 dead 1 growing 2 mature
    private int cropAmount;
}
