package com.github.ltprc.gamepal.model.lobby;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfo {
    private BigDecimal maxSpeedX; // block per second
    private BigDecimal maxSpeedY; // block per second
    private BigDecimal accelerationX; // block per second square
    private BigDecimal accelerationY; // block per second square
    private int hpMax;
    private int hp;
    private int vpMax;
    private int vp;
    private int hunger;
    private int hungerMax;
    private int thirst;
    private int thirstMax;
    private int level;
    private int exp;
    private int expMax;
    private int money;
    private Map<String, Integer> items = new ConcurrentHashMap<>(); // itemId, amount
    private BigDecimal capacity;
    private BigDecimal capacityMax;
    private Map<String, Integer> preservedItems = new ConcurrentHashMap<>(); // itemId, amount
    private Set<String> buff = new ConcurrentSkipListSet<>();
}
