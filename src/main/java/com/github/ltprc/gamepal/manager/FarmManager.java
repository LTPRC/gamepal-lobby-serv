package com.github.ltprc.gamepal.manager;

import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

import java.util.Optional;

public interface FarmManager {

    void updateFarmStatus(GameWorld world);

    void plant(GameWorld world, String userCode, String farmId, String corpCode);

    void gather(GameWorld world, String userCode, String farmId);

    Optional<Block> generateCropByFarm(GameWorld world, Block farmBlock);
}
