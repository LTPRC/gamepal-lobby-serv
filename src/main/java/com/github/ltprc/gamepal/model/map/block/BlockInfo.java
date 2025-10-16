package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
public class BlockInfo {
    int type;
    String id;
    int code;
    Structure structure;
    AtomicInteger hpMax = new AtomicInteger(BlockConstants.HP_DEFAULT);
    AtomicInteger hp = new AtomicInteger(BlockConstants.HP_DEFAULT);

    public BlockInfo(BlockInfo blockInfo) {
        if (null == blockInfo) {
            return;
        }
        type = blockInfo.type;
        id = blockInfo.id;
        code = blockInfo.code;
        structure = new Structure(blockInfo.structure);
        hpMax = blockInfo.hpMax;
        hp = blockInfo.hp;
    }

    public BlockInfo(Integer type, String id, int code, Structure structure) {
        this.type = type;
        this.id = id;
        this.code = code;
        this.structure = new Structure(structure.getMaterial(), structure.getLayer(), structure.getShape(),
                structure.getImageSize());
    }
}
