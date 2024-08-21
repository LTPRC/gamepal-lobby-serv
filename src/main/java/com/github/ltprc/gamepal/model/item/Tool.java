package com.github.ltprc.gamepal.model.item;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Tool extends Item {
    // 0 - 默认共用
    private int itemIndex;
    // 0 - 默认
    private int toolType;
    private int toolMode;
    private int useTime;
    private int ammoAmount;
    private int ammoAmountMax;
    private int reloadAmount;
    private int reloadTime;
    private String ammoCode;
}
