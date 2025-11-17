package com.github.ltprc.gamepal.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public interface SceneManager {
    void fillScene(final GameWorld world, final Region region, final IntegerCoordinate sceneCoordinate);
    Queue<Block> collectSurroundingBlocks(final GameWorld world, final Block player, final int sceneScanRadius);
    List<Block> collectLinearBlocks(final GameWorld world, WorldCoordinate fromWorldCoordinate, Block eventBlock,
                                    String sourceId);
    int[][] collectGridsByUserCode(final String userCode, final int sceneScanRadius);
    BigDecimal[][] collectAltitudesByUserCode(final String userCode, final int sceneScanRadius);
    JSONObject convertBlock2OldBlockInstance(final GameWorld world, final String userCode, final Block block,
                                             final boolean useWorldCoordinate, final long timestamp);
    Block addDropBlock(final GameWorld world, final WorldCoordinate worldCoordinate,
                       final Map.Entry<String, Integer> drop);
    Block addTeleportBlock(final GameWorld world, final WorldCoordinate worldCoordinate, final int blockCode,
                           final WorldCoordinate to);
    Block addTextDisplayBlock(final GameWorld world, final WorldCoordinate worldCoordinate, final int blockCode,
                              final String textDisplay);
    Block addOtherBlock(final GameWorld world, final WorldCoordinate worldCoordinate, final int blockCode);
    boolean checkBlockSpace2Build(final GameWorld world, final Block block);
    void removeBlock(GameWorld world, Block block, boolean isDestroyed);
    int getGridBlockCode(final GameWorld world, final WorldCoordinate worldCoordinate);
    void setGridBlockCode(final GameWorld world, final WorldCoordinate worldCoordinate, final int code);
    BigDecimal getAltitude(final GameWorld world, final WorldCoordinate worldCoordinate);
    void updateBlockAltitude(final GameWorld world, Block block);
}
