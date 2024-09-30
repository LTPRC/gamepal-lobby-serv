package com.github.ltprc.gamepal.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

import java.util.Queue;

public interface SceneManager {

    Region generateRegion(final int regionNo);

    void fillScene(final GameWorld world, final Region region, final IntegerCoordinate sceneCoordinate);

    Queue<Block> collectBlocksByUserCode(final GameWorld world, final Block player, final int sceneScanRadius);

    Queue<Block> collectBlocksFromScenes(final GameWorld world, final Block player, final int sceneScanRadius);

    Queue<Block> collectBlocksFromPlayerInfoMap(final GameWorld world, final Block player, final int sceneScanRadius);

    int[][] collectGridsByUserCode(final String userCode, final int sceneScanRadius);

    JSONObject convertBlock2OldBlockInstance(final GameWorld world, final Block block, final boolean useWorldCoordinate);
}
