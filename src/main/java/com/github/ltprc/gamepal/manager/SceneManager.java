package com.github.ltprc.gamepal.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

import java.util.Map;
import java.util.Queue;

public interface SceneManager {
    Region generateRegion(final int regionNo);
    void fillScene(final GameWorld world, final Region region, final IntegerCoordinate sceneCoordinate);
    Queue<Block> collectBlocks(final GameWorld world, final Block player, final int sceneScanRadius);
    Queue<Block> collectBlocksFromScenes(final GameWorld world, final Block player, final int sceneScanRadius);
    Queue<Block> collectBlocksFromCreatureMap(final GameWorld world, final Block player, final int sceneScanRadius);
    int[][] collectGridsByUserCode(final String userCode, final int sceneScanRadius);
    JSONObject convertBlock2OldBlockInstance(final GameWorld world, final String userCode, final Block block,
                                             final boolean useWorldCoordinate);
    Block addDropBlock(final GameWorld world, final WorldCoordinate worldCoordinate,
                       final Map.Entry<String, Integer> drop);
    Block addTeleportBlock(final GameWorld world, final int code, final WorldCoordinate worldCoordinate,
                           final WorldCoordinate to);
    Block addOtherBlock(final GameWorld world, final WorldCoordinate worldCoordinate, final int blockCode);
    boolean checkBlockSpace2Build(final GameWorld world, final Block block);
    void removeBlock(GameWorld world, Block block, boolean isDestroyed);
    int getGridBlockCode(final GameWorld world, final WorldCoordinate worldCoordinate);
    void setGridBlockCode(final GameWorld world, final WorldCoordinate worldCoordinate, final int code);
}
