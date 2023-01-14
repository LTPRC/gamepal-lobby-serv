package com.github.ltprc.gamepal.model.lobby;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class PlayerStatus {
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
    private Map<String, Integer> items = new HashMap<>(); // itemId, amount
    private BigDecimal capacity;
    private BigDecimal capacityMax;
    private Map<String, Integer> preservedItems = new HashMap<>(); // itemId, amount

    private Set<String> buff = new HashSet<>();
}
