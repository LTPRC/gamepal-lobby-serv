package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block extends Coordinate {
    private Integer type;
    private String id;
    private String code;

    public Block(Block block) {
        super(block);
        type = block.type;
        id = block.id;
        code = block.code;
    }

    public Block(Integer type, String id, String code, Coordinate coordinate) {
        super(coordinate);
        this.type = type;
        this.id = id;
        this.code = code;
    }
}
