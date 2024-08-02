package com.github.ltprc.gamepal.model.creature;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class PrivateInfo {
    private String id;
    private BigDecimal capacity;
    private BigDecimal capacityMax;
    private Map<String, Integer> items = new ConcurrentHashMap<>(); // itemId, amount
    private Map<String, Integer> preservedItems = new ConcurrentHashMap<>(); // itemId, amount
}
