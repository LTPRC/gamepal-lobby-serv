package com.github.ltprc.gamepal.model.creature;

import com.github.ltprc.gamepal.config.GamePalConstants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class BagInfo {
    private String id;
    private BigDecimal capacity = BigDecimal.ZERO;
    private BigDecimal capacityMax = BigDecimal.valueOf(GamePalConstants.CAPACITY_MAX);
    private Map<String, Integer> items = new ConcurrentHashMap<>(); // itemId, amount
}
