package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.BlockConstants;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class BlockInfo {

    private int type;
    private String id;
    private int code;
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
        hpMax = blockInfo.hpMax;
        hp = blockInfo.hp;
        timeCreated = blockInfo.timeCreated;
        timeUpdated = blockInfo.timeUpdated;
    }

    public BlockInfo(Integer type, String id, int code, long timestamp) {
        this.type = type;
        this.id = id;
        this.code = code;
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

    public void setHpMax(int hpMax, long timestamp) {
        this.hpMax.set(hpMax);
        setTimeUpdated(timestamp);
    }

    public void setHp(int hp, long timestamp) {
        this.hp.set(hp);
        setTimeUpdated(timestamp);
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public void setTimeUpdated(long timeUpdated) {
        this.timeUpdated = timeUpdated;
    }
}
