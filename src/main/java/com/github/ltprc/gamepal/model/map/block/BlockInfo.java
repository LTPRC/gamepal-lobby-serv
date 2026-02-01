package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.config.BlockConstants;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class BlockInfo {

    private int type;
    private String id;
    private int code;
    private AtomicInteger hpMax = new AtomicInteger(BlockConstants.HP_DEFAULT);
    private AtomicInteger hp = new AtomicInteger(BlockConstants.HP_DEFAULT);
    private long timeCreated;
    private long timeUpdated;
    private int frame;
    private int frameMax;
    private int period;

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
        frame = blockInfo.getFrame();
        frameMax = blockInfo.getFrameMax();
        period = blockInfo.getPeriod();
    }

    public BlockInfo(Integer type, String id, int code, long timestamp) {
        this.type = type;
        this.id = id;
        this.code = code;
        setTimeCreated(timestamp);
        setTimeUpdated(timestamp);
    }
}
