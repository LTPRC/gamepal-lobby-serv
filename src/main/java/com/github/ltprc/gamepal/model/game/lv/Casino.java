package com.github.ltprc.gamepal.model.game.lv;

import com.github.ltprc.gamepal.model.game.Cash;
import lombok.Data;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
public class Casino {
    private int casinoNo;
    private Queue<Cash> cashQueue = new ConcurrentLinkedQueue<>();
    private Map<Integer, Integer> diceMap = new ConcurrentHashMap<>();
}
