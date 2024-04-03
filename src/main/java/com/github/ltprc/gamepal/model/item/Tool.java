package com.github.ltprc.gamepal.model.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool extends Item {
    // 0 - 默认共用
    private int itemIndex;
    // 0 - 默认
    private int itemType;
    private int itemMode;
    private int itemTime;
}
