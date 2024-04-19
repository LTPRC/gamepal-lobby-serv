package com.github.ltprc.gamepal.model;

import com.github.ltprc.gamepal.model.map.world.WorldMovingBlock;
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
public class PlayerInfo extends WorldMovingBlock {

    // Basic properties
    private int playerType; // 0-human 1-AI

    // Character properties
    private String avatar;
    private String firstName;
    private String lastName;
    private String nickname;
    private String nameColor;
    private String creature;
    private String gender;
    private String skinColor;
    private String hairstyle;
    private String hairColor;
    private String eyes;
    private int[] faceCoefs;

    // Dynamic properties
    private int playerStatus;
    private BigDecimal maxSpeed; // block per frame
    private BigDecimal acceleration; // block per frame square
    private Set<String> tools = new ConcurrentSkipListSet<>();
    private Set<String> outfits = new ConcurrentSkipListSet<>();
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
    private int[] buff; // buff code, remaining frame
    private int[][] skill; // skillNo; skillCode, skillMode(0=semi-auto 1=auto), time, timeMax
    private String bossId;
    private BigDecimal playerViewRadius;
}
