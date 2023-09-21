package com.github.ltprc.gamepal.model.game;

import lombok.Data;

@Data
public class Player {
    private int playerNo;
    private String id; // Not userCode, but terminal id 23/09/14
    private String name;
}
