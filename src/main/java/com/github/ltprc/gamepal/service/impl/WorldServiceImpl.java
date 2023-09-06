package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.map.Coordinate;
import com.github.ltprc.gamepal.model.map.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.Region;
import com.github.ltprc.gamepal.model.map.Scene;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldBlock;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.model.map.world.WorldTeleport;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Transactional
@Service
public class WorldServiceImpl implements WorldService {

    private static final Log logger = LogFactory.getLog(WorldServiceImpl.class);
    private Map<String, GameWorld> worldMap = new LinkedHashMap<>(); // worldCode, world
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
        GameWorld world =worldMap.get(worldCode);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1019));
        }
        world.getOnlineMap().entrySet().forEach(entry -> {
            userService.logoff(entry.getKey(), "", false);
        });
        worldMap.remove(worldCode);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public Map<Integer, Region> getRegionMap() {
        return regionMap;
    }

    private void initiateWorld(GameWorld world) {
        world.setSessionMap(new ConcurrentHashMap<>()); // userCode, session
        world.setTokenMap(new ConcurrentHashMap<>()); // userCode, token
        world.setOnlineMap(new ConcurrentHashMap<>()); // userCode, timestamp
        world.setBlockMap(new ConcurrentSkipListMap<>());
        loadBlocks(world);
    }

    @Override
    public void loadScenes() {
        JSONArray regions = ContentUtil.jsonFile2JSONArray("src/main/resources/json/regions.json");
        for (Object obj : regions) {
            JSONObject region = JSON.parseObject(String.valueOf(obj));
            Region newRegion = new Region();
            int regionNo = region.getInteger("regionNo");
            int height = region.getInteger("height");
            int width = region.getInteger("width");
            newRegion.setRegionNo(regionNo);
            newRegion.setHeight(height);
            newRegion.setWidth(width);
            JSONArray scenes = region.getJSONArray("scenes");
            for (Object obj2 : scenes) {
                JSONObject scene = JSON.parseObject(String.valueOf(obj2));
                Scene newScene = new Scene();
                String name = scene.getString("name");
                int y = scene.getInteger("y");
                int x = scene.getInteger("x");
                newScene.setName(name);
                newScene.setSceneCoordinate(new IntegerCoordinate(x, y));
                newScene.setBlocks(new HashMap<>());
                JSONArray map = scene.getJSONArray("map");
                for (int i = 0; i < Math.min(height, map.size()); i++) {
                    JSONArray blockRow = map.getJSONArray(i);
                    for (int j = 0; j < Math.min(width, blockRow.size()); j++) {
                        Integer value = blockRow.getInteger(j);
                        newScene.getBlocks().put(new IntegerCoordinate(j, i), value);
                    }
                }
                if (null == newRegion.getScenes()) {
                    newRegion.setScenes(new HashMap<>());
                }
                newRegion.getScenes().put(newScene.getSceneCoordinate(), newScene);
            }
            regionMap.put(regionNo, newRegion);
        }
    }

    @Override
    public void loadBlocks(GameWorld world) {
        WorldBlock block;
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(1)));
        block.setType(GamePalConstants.BLOCK_TYPE_BED);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3007");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(3), BigDecimal.valueOf(1)));
        block.setType(GamePalConstants.BLOCK_TYPE_TOILET);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3008");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(1)));
        block.setType(GamePalConstants.BLOCK_TYPE_DRESSER);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3010");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(7), BigDecimal.valueOf(1)));
        block.setType(GamePalConstants.BLOCK_TYPE_WORKSHOP);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3009");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(4)));
        block.setType(GamePalConstants.BLOCK_TYPE_GAME);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3021");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(3), BigDecimal.valueOf(4)));
        block.setType(GamePalConstants.BLOCK_TYPE_STORAGE);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3001");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(4)));
        block.setType(GamePalConstants.BLOCK_TYPE_COOKER);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3004");
        world.getBlockMap().put(block.getId(), block);
        block = new WorldBlock();
        block.setRegionNo(1);
        block.setSceneCoordinate(new IntegerCoordinate(-1, 0));
        block.setCoordinate(new Coordinate(BigDecimal.valueOf(7), BigDecimal.valueOf(4)));
        block.setType(GamePalConstants.BLOCK_TYPE_SINK);
        block.setId(UUID.randomUUID().toString());
        block.setCode("3005");
        world.getBlockMap().put(block.getId(), block);

        WorldTeleport worldTeleport;
        WorldCoordinate to;
        worldTeleport = new WorldTeleport();
        worldTeleport.setRegionNo(1);
        worldTeleport.setSceneCoordinate(new IntegerCoordinate(0, 0));
        worldTeleport.setCoordinate(new Coordinate(BigDecimal.valueOf(4), BigDecimal.valueOf(1)));
        worldTeleport.setType(GamePalConstants.BLOCK_TYPE_TELEPORT);
        worldTeleport.setId(UUID.randomUUID().toString());
        worldTeleport.setCode("2212");
        to = new WorldCoordinate();
        to.setRegionNo(worldTeleport.getRegionNo());
        to.setSceneCoordinate(new IntegerCoordinate(0, 1));
        to.setCoordinate(new Coordinate(BigDecimal.valueOf(6), BigDecimal.valueOf(0.5)));
        worldTeleport.setTo(to);
        world.getBlockMap().put(worldTeleport.getId(), worldTeleport);
        worldTeleport = new WorldTeleport();
        worldTeleport.setRegionNo(1);
        worldTeleport.setSceneCoordinate(new IntegerCoordinate(0, 0));
        worldTeleport.setCoordinate(new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(1)));
        worldTeleport.setType(GamePalConstants.BLOCK_TYPE_TELEPORT);
        worldTeleport.setId(UUID.randomUUID().toString());
        worldTeleport.setCode("2204");
        to = new WorldCoordinate();
        to.setRegionNo(worldTeleport.getRegionNo());
        to.setSceneCoordinate(new IntegerCoordinate(0, -1));
        to.setCoordinate(new Coordinate(BigDecimal.valueOf(3), BigDecimal.valueOf(0.5)));
        worldTeleport.setTo(to);
        world.getBlockMap().put(worldTeleport.getId(), worldTeleport);
        worldTeleport = new WorldTeleport();
        worldTeleport.setRegionNo(1);
        worldTeleport.setSceneCoordinate(new IntegerCoordinate(0, -1));
        worldTeleport.setCoordinate(new Coordinate(BigDecimal.valueOf(4), BigDecimal.valueOf(1)));
        worldTeleport.setType(GamePalConstants.BLOCK_TYPE_TELEPORT);
        worldTeleport.setId(UUID.randomUUID().toString());
        worldTeleport.setCode("2212");
        to = new WorldCoordinate();
        to.setRegionNo(worldTeleport.getRegionNo());
        to.setSceneCoordinate(new IntegerCoordinate(0, 0));
        to.setCoordinate(new Coordinate(BigDecimal.valueOf(6), BigDecimal.valueOf(0.5)));
        worldTeleport.setTo(to);
        world.getBlockMap().put(worldTeleport.getId(), worldTeleport);
        worldTeleport = new WorldTeleport();
        worldTeleport.setRegionNo(1);
        worldTeleport.setSceneCoordinate(new IntegerCoordinate(0, 1));
        worldTeleport.setCoordinate(new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(1)));
        worldTeleport.setType(GamePalConstants.BLOCK_TYPE_TELEPORT);
        worldTeleport.setId(UUID.randomUUID().toString());
        worldTeleport.setCode("2204");
        to = new WorldCoordinate();
        to.setRegionNo(worldTeleport.getRegionNo());
        to.setSceneCoordinate(new IntegerCoordinate(0, 0));
        to.setCoordinate(new Coordinate(BigDecimal.valueOf(3), BigDecimal.valueOf(0.5)));
        worldTeleport.setTo(to);
        world.getBlockMap().put(worldTeleport.getId(), worldTeleport);
    }
}
