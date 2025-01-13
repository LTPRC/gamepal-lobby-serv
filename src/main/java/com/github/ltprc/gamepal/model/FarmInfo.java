package com.github.ltprc.gamepal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmInfo {
    private String cropCode;
    private int cropStatus;
    private int cropAmount;
    private int cropFrame;
}
