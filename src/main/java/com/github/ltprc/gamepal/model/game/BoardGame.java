package com.github.ltprc.gamepal.model.game;

import lombok.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BoardGame {
    private String id;
    private int type;
    private int status;
    private int gameNumber; // Current game No
    private int roundNumber; // Current round No
    private int PlayerNumber; // Current player's No
    private int minPlayerNum;
    private int maxPlayerNum;
    private Set<String> standbySet = new HashSet<>();
    private Map<Integer, Player> playerMap = new ConcurrentHashMap<>();
}
