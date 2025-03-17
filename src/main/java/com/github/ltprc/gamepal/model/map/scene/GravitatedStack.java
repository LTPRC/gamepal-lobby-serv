package com.github.ltprc.gamepal.model.map.scene;

import com.github.ltprc.gamepal.model.map.block.Block;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Stack;

@Data
public class GravitatedStack {

    private BigDecimal minAltitude;
    private BigDecimal maxAltitude;
    private Stack<Block> stack;

    public GravitatedStack(BigDecimal altitude) {
        minAltitude = altitude;
        maxAltitude = altitude;
        stack = new Stack<>();
    }

    public void addBlock(Block block) {
        // TODO Verify blockType
        if (stack.stream()
                .noneMatch(block1 -> StringUtils.equals(block.getBlockInfo().getId(), block1.getBlockInfo().getId()))) {
            stack.push(block);
            block.getBlockInfo().getStructure().getShape().getRadius().setZ(maxAltitude);
            maxAltitude = maxAltitude.add(block.getBlockInfo().getStructure().getShape().getRadius().getZ());
        }
    }

    public Block removeBlock(String id) {
        Stack<Block> tempStack = new Stack<>();
        Block topBlock = null;
        while (!stack.empty()) {
            topBlock = stack.pop();
            if (StringUtils.equals(id, topBlock.getBlockInfo().getId())) {
                maxAltitude = maxAltitude.subtract(topBlock.getBlockInfo().getStructure().getShape().getRadius().getZ());
                break;
            } else {
                tempStack.push(topBlock);
            }
        }
        while (!tempStack.empty()) {
            stack.push(tempStack.pop());
        }
        return topBlock;
    }
}
