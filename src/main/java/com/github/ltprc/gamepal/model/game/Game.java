package com.github.ltprc.gamepal.model.game;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Game {
    private String userCode;
    // 0: start
    // 1: wait for players
    // 2: in-game
    // 3: end
    private int gameStatus;
    private int minPlayerNum;
    private int maxPlayerNum;
    private Map<Integer, String> userCodeMap = new ConcurrentHashMap<>();
    private Map<Integer, Player> playerMap = new ConcurrentHashMap<>();
}
