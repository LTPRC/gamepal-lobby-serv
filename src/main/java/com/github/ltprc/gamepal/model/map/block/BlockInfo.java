package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class BlockInfo {

    private int type;
    private String id;
    private int code;
    private Structure structure;
    private AtomicInteger hpMax = new AtomicInteger(BlockConstants.HP_DEFAULT);
    private AtomicInteger hp = new AtomicInteger(BlockConstants.HP_DEFAULT);
    private long timeCreated;
    private long timeUpdated;

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
        timeCreated = blockInfo.timeCreated;
        timeUpdated = blockInfo.timeUpdated;
    }

    public BlockInfo(Integer type, String id, int code, Structure structure, long timestamp) {
        this.type = type;
        this.id = id;
        this.code = code;
        this.structure = new Structure(structure.getMaterial(), structure.getLayer(), structure.getShape(),
                structure.getImageSize());
        setTimeCreated(timestamp);
        setTimeUpdated(timestamp);
    }

    public void setType(int type, long timestamp) {
        this.type = type;
        setTimeUpdated(timestamp);
    }

    public void setId(String id, long timestamp) {
        this.id = id;
        setTimeUpdated(timestamp);
    }

    public void setCode(int code, long timestamp) {
        this.code = code;
        setTimeUpdated(timestamp);
    }

    public void setStructure(Structure structure, long timestamp) {
        this.structure = structure;
        setTimeUpdated(timestamp);
    }

    public void setHpMax(int hpMax, long timestamp) {
        this.hpMax = new AtomicInteger(hpMax);
        setTimeUpdated(timestamp);
    }

    public void setHp(int hp, long timestamp) {
        this.hp = new AtomicInteger(hp);
        setTimeUpdated(timestamp);
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public void setTimeUpdated(long timeUpdated) {
        this.timeUpdated = timeUpdated;
    }
}
