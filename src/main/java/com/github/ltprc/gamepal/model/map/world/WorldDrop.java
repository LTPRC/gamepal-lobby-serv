package com.github.ltprc.gamepal.model.map.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldDrop extends WorldBlock {
    private String itemNo;
    private int amount;

    public WorldDrop(String itemNo, int amount, WorldBlock worldBlock) {
        super(worldBlock);
        this.itemNo = itemNo;
        this.amount = amount;
    }
}
