package com.github.ltprc.gamepal.model.creature;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
@NoArgsConstructor
public class PlayerInfo extends CreatureInfo {

    // Basic properties
    private int playerType; // 0-human 1-AI
    private int playerStatus;

    // Character properties
    private String avatar;
    private String firstName;
    private String lastName;
    private String nickname;
    private String nameColor;
    private int hairstyle;
    private int hairColor;
    private int eyes;
    private int[] faceCoefs;

    // Dynamic properties
    private int level;
    private int exp;
    private int expMax;
    private int money;
    private Set<String> tools = new ConcurrentSkipListSet<>();
    private Set<String> outfits = new ConcurrentSkipListSet<>();
    private Map<String, Integer> items = new ConcurrentHashMap<>(); // itemId, amount
    private BigDecimal capacity;
    private BigDecimal capacityMax;
    private Map<String, Integer> preservedItems = new ConcurrentHashMap<>(); // itemId, amount
    private String bossId;
    private String topBossId;
}
