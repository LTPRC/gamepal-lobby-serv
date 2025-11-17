package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class BlockFactory {

    private BlockFactory() {}

    public static Queue<Block> createDistanceRankingQueue(final RegionInfo regionInfo,
                                                          final WorldCoordinate worldCoordinate) {
        return new PriorityQueue<>(Comparator.comparing(o ->
                BlockUtil.calculateDistance(regionInfo, worldCoordinate, o.getWorldCoordinate())));
    }
}
