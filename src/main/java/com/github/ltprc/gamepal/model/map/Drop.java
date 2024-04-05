package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drop extends Block {
    private String itemNo;
    private int amount;

    public Drop(Drop drop) {
        super(drop);
        itemNo = drop.itemNo;
        amount = drop.amount;
    }

    public Drop(String itemNo, int amount, Block block) {
        super(block);
        this.itemNo = itemNo;
        this.amount = amount;
    }
}
