package com.github.ltprc.gamepal.model.item;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class Consumable extends Item {
    private Map<String, Integer> effects = new HashMap<>();
}
