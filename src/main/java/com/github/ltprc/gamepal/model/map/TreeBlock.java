package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeBlock extends Block {
    private int treeType;
    private int treeHeight;
    private BigDecimal radius;

    public TreeBlock(TreeBlock treeBlock) {
        super(treeBlock);
        treeType = treeBlock.treeType;
        treeHeight = treeBlock.treeHeight;
        radius = treeBlock.radius;
    }
}
