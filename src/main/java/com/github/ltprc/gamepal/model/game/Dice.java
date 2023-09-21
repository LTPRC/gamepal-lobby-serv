package com.github.ltprc.gamepal.model.game;

import lombok.Data;

@Data
public class Dice {

    private int point;

    public Dice() {
        point = 0;
    }
}
