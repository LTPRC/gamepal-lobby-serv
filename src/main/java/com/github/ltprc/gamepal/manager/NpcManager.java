package com.github.ltprc.gamepal.manager;


import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface NpcManager {

    String createNpc(GameWorld world);

    void putNpc(String userCode, String npcUserCode);
}
