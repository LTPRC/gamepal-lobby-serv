package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drop {

    private String itemNo;
    private int amount;
    private int sceneNo;
    private Coordinate position;
}
