package com.github.ltprc.gamepal.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Junk extends Item {
    private Map<String, Integer> materials = new HashMap<>();
}
