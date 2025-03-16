package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class BlockFactory {

    private BlockFactory() {}

    public static Queue<Block> createYRankingQueue(final RegionInfo regionInfo) {
        return new PriorityQueue<>((o1, o2) -> {
            if (!Objects.equals(o1.getBlockInfo().getStructure().getLayer() / 10,
                    o2.getBlockInfo().getStructure().getLayer() / 10)) {
                return o1.getBlockInfo().getStructure().getLayer() / 10
                        - o2.getBlockInfo().getStructure().getLayer() / 10;
            }
            BigDecimal verticalDistance = BlockUtil.calculateVerticalDistance(regionInfo, o1.getWorldCoordinate(),
                    o2.getWorldCoordinate());
            if (null != verticalDistance && !verticalDistance.equals(BigDecimal.ZERO)) {
                return BigDecimal.ZERO.compareTo(verticalDistance);
            }
            BigDecimal horizontalDistance = BlockUtil.calculateHorizontalDistance(regionInfo, o1.getWorldCoordinate(),
                    o2.getWorldCoordinate());
            if (null != horizontalDistance && !horizontalDistance.equals(BigDecimal.ZERO)) {
                return BigDecimal.ZERO.compareTo(horizontalDistance);
            }
            int layerDiff = o1.getBlockInfo().getStructure().getLayer() % 10
                    - o2.getBlockInfo().getStructure().getLayer() % 10;
            if (layerDiff != 0) {
                return layerDiff;
            }
            return o1.getBlockInfo().getCode() - o2.getBlockInfo().getCode();
        });
    }

    public static Queue<Block> createDistanceRankingQueue(final RegionInfo regionInfo,
                                                          final WorldCoordinate worldCoordinate) {
        return new PriorityQueue<>(Comparator.comparing(o ->
                BlockUtil.calculateDistance(regionInfo, worldCoordinate, o.getWorldCoordinate())));
    }
}
