package com.github.ltprc.gamepal.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Outfit extends Item {
    // 0 - 默认共用
    private int itemIndex;
}
