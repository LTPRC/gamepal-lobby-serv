package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.model.world.GameWorld;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.map.Scene;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

@Transactional
@Service
public class WorldServiceImpl implements WorldService {

    private static final Log logger = LogFactory.getLog(WorldServiceImpl.class);
    private Map<String, GameWorld> worldMap = new LinkedHashMap<>(); // worldCode, world
    @Value("${json.regions}")
    private String regionsJson;
    private Map<Integer, Region> regionMap = new HashMap<>(); // regionNo, region

    @Autowired
    private UserService userService;

    @Override
    public Map<String, GameWorld> getWorldMap() {
        return worldMap;
    }

    @Override
    public ResponseEntity<String> addWorld(String worldCode) {
        JSONObject rst = ContentUtil.generateRst();
        if (worldMap.containsKey(worldCode)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1015));
        }
        GameWorld world = new GameWorld();
        initiateWorld(world);
        worldMap.put(worldCode, world);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> removeWorld(String worldCode) {
        JSONObject rst = ContentUtil.generateRst();
        worldMap.get(worldCode).getTokenMap().entrySet().forEach(entry -> {
            userService.logoff(entry.getKey(), entry.getValue());
        });
        worldMap.remove(worldCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public Map<Integer, Region> getRegionMap() {
        return regionMap;
    }

    @Override
    public void initiateWorld(GameWorld world) {
        world.setSessionMap(new ConcurrentHashMap<>()); // userCode, session
        world.setTokenMap(new ConcurrentHashMap<>()); // userCode, token
        world.setOnlineMap(new ConcurrentHashMap<>()); // userCode, timestamp
        world.setOnlineQueue(new PriorityQueue<>((s1, s2) -> {
            long l1 = world.getOnlineMap().get(s1);
            long l2 = world.getOnlineMap().get(s2);
            if (l1 - l2 < 0) {
                return -1;
            } else if (l1 - l2 > 0) {
                return 1;
            } else {
                return 0;
            }
        })); // userCode
    }

    public void loadScenes() {
        JSONArray regions = JSON.parseArray(regionsJson);
        for (Object obj : regions) {
            JSONObject region = (JSONObject) obj;
            Region newRegion = new Region();
            int regionNo = region.getInteger("regionNo");
            int height = region.getInteger("height");
            int width = region.getInteger("width");
            newRegion.setRegionNo(regionNo);
            newRegion.setHeight(height);
            newRegion.setWidth(width);
            JSONArray scenes = region.getJSONArray("scenes");
            for (Object obj2 : scenes) {
                JSONObject scene = (JSONObject) obj2;
                Scene newScene = new Scene();
                String name = scene.getString("name");
                int y = scene.getInteger("y");
                int x = scene.getInteger("x");
                newScene.setName(name);
                newScene.setY(y);
                newScene.setX(x);
                newScene.setBlocks(new HashMap<>());
                JSONArray blocks = scene.getJSONArray("blocks");
                for (int i = 0; i < Math.min(y, blocks.size()); i++) {
                    JSONArray blockRow = blocks.getJSONArray(i);
                    for (int j = 0; j < Math.min(x, blockRow.size()); j++) {
                        Integer value = blockRow.getInteger(j);
                        newScene.getBlocks().put(new IntegerCoordinate(i, j), value);
                    }
                }
                newScene.setTerrain(new HashMap<>());
                JSONArray terrain = scene.getJSONArray("terrain");
                for (int i = 0; i < Math.min(y, terrain.size()); i++) {
                    JSONArray terrainRow = terrain.getJSONArray(i);
                    for (int j = 0; j < Math.min(x, terrainRow.size()); j++) {
                        Integer value = terrainRow.getInteger(j);
                        newScene.getTerrain().put(new IntegerCoordinate(i, j), value);
                    }
                }
                if (null == newRegion.getScenes()) {
                    newRegion.setScenes(new HashMap<>());
                }
                newRegion.getScenes().put(new IntegerCoordinate(y, x), newScene);
            }
            regionMap.put(regionNo, newRegion);
        }
    }
}
