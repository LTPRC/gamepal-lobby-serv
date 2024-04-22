package com.github.ltprc.gamepal.manager;


import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.map.world.GameWorld;

public interface NpcManager {

    String createNpc(GameWorld world);

    void putNpc(String userCode, String npcUserCode);

    JSONObject changeNpcBehavior(JSONObject request);

    JSONObject runNpcTask(JSONObject request);

    void updateNpcBrains(GameWorld world);

    void resetNpcBrainQueues(String npcUserCode);
}
