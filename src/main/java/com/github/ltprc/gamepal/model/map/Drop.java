package com.github.ltprc.gamepal.model.map;

import lombok.Data;

@Data
public class Drop {

    private String itemNo;
    private int amount;
    private int sceneNo;
    private Coordinate position;
}
