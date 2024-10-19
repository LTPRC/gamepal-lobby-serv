package com.github.ltprc.gamepal.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    private String recipeNo;
    private int type; // block type
    private Map<String, Integer> cost;
    private Map<String, Integer> value;
}
