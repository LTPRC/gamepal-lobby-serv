package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.Block;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;

import java.util.Queue;

public interface SceneManager {

    Region generateRegion(final int regionNo);

    void fillScene(final Region region, final IntegerCoordinate sceneCoordinate, int regionIndex);

    Queue<Block> collectBlocksByUserCode(final String userCode);
}
