package com.github.ltprc.gamepal.model.game;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class Game {
    private int number;
    private int type;
    private int status;
    private int playerCountMin;
    private int playerCountMax;
    private Map<String, Integer> playerMap = new ConcurrentHashMap<>(); // userCode, playerStatus

    public Game(int number, int type, int status, int playerCountMin, int playerCountMax) {
        this.number = number;
        this.type = type;
        this.status = status;
        this.playerCountMin = playerCountMin;
        this.playerCountMax = playerCountMax;
    }
}
