package com.github.ltprc.gamepal.model.map.scene;

import com.github.ltprc.gamepal.model.map.block.Block;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Stack;

@Data
public class GravitatedStack {

    private BigDecimal minAltitude;
    private BigDecimal maxAltitude;
    private Stack<Block> stack = new Stack<>();

    public GravitatedStack(BigDecimal altitude) {
        minAltitude = altitude;
        maxAltitude = altitude;
    }
}
