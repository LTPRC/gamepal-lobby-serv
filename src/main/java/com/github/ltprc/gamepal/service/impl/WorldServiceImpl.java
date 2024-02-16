package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.item.Consumable;
import com.github.ltprc.gamepal.model.item.Item;
import com.github.ltprc.gamepal.model.item.Junk;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.PlayerUtil;
import com.github.ltprc.gamepal.util.lv.LasVegasGameUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

@Transactional
@Service
public class WorldServiceImpl implements WorldService {

    private static final Log logger = LogFactory.getLog(WorldServiceImpl.class);
    private Map<String, GameWorld> worldMap = new LinkedHashMap<>(); // worldCode, world (We only allow 1 world now 24/02/16)
    private Map<Integer, Region> regionMap = new HashMap<>(); // regionNo, region
    private Map<String, Item> itemMap = new HashMap<>(); // itemNo, item

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

    @Override
    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    private void initiateWorld(GameWorld world) {
        world.setSessionMap(new ConcurrentHashMap<>()); // userCode, session
        world.setTokenMap(new ConcurrentHashMap<>()); // userCode, token
        world.setOnlineMap(new ConcurrentHashMap<>()); // userCode, timestamp
        world.setBlockMap(new ConcurrentSkipListMap<>());
        world.setGameMap(new ConcurrentHashMap<>());
        world.setEventQueue(new ConcurrentLinkedQueue<>());
        loadBlocks(world);
        initiateGame(world);
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
                newScene.setBlocks(new ArrayList<>());
                newScene.setEvents(new ArrayList<>());
                JSONArray map = scene.getJSONArray("map");
                if (null != map && !map.isEmpty()) {
                    for (int i = 0; i < Math.min(height, map.size()); i++) {
                        JSONArray blockRow = map.getJSONArray(i);
                        for (int j = 0; j < Math.min(width, blockRow.size()); j++) {
                            Integer value = blockRow.getInteger(j);
                            Block block = new Block();
                            switch (value / 10000) {
                                case 1:
                                default:
                                    block.setType(GamePalConstants.BLOCK_TYPE_GROUND);
                                    break;
                                case 2:
                                    block.setType(GamePalConstants.BLOCK_TYPE_WALL);
                                    break;
                                case 3:
                                    block.setType(GamePalConstants.BLOCK_TYPE_CEILING);
                                    break;
                            }
                            block.setCode(String.valueOf(value % 10000));
                            block.setX(BigDecimal.valueOf(j));
                            block.setY(BigDecimal.valueOf(i));
                            newScene.getBlocks().add(block);
                        }
                    }
                }
                JSONArray blocks = scene.getJSONArray("blocks");
                if (null != blocks && !blocks.isEmpty()) {
                    for (int i = 0; i < blocks.size(); i++) {
                        JSONArray blockRow = blocks.getJSONArray(i);
                        Integer type = blockRow.getInteger(0);
                        switch (type) {
                            case GamePalConstants.BLOCK_TYPE_DROP:
                                Drop drop = new Drop();
                                drop.setType(type);
                                drop.setCode(String.valueOf(blockRow.getInteger(1)));
                                drop.setX(BigDecimal.valueOf(blockRow.getInteger(2)));
                                drop.setY(BigDecimal.valueOf(blockRow.getInteger(3)));
                                drop.setItemNo(blockRow.getString(4));
                                drop.setAmount(blockRow.getInteger(5));
                                newScene.getBlocks().add(drop);
                                break;
                            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                                Teleport teleport = new Teleport();
                                teleport.setType(type);
                                teleport.setCode(String.valueOf(blockRow.getInteger(1)));
                                teleport.setX(BigDecimal.valueOf(blockRow.getInteger(2)));
                                teleport.setY(BigDecimal.valueOf(blockRow.getInteger(3)));
                                WorldCoordinate to = new WorldCoordinate();
                                to.setRegionNo(blockRow.getInteger(4));
                                to.setSceneCoordinate(new IntegerCoordinate(blockRow.getInteger(5), blockRow.getInteger(6)));
                                to.setCoordinate(new Coordinate(BigDecimal.valueOf(blockRow.getInteger(7)), BigDecimal.valueOf(blockRow.getInteger(8))));
                                teleport.setTo(to);
                                newScene.getBlocks().add(teleport);
                                break;
                            default:
                                Block block = new Block();
                                block.setType(type);
                                block.setCode(String.valueOf(blockRow.getInteger(1)));
                                block.setX(BigDecimal.valueOf(blockRow.getInteger(2)));
                                block.setY(BigDecimal.valueOf(blockRow.getInteger(3)));
                                newScene.getBlocks().add(block);
                                break;
                        }
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
        regionMap.entrySet().stream().forEach(entry1 -> {
            Region region = entry1.getValue();
            region.getScenes().entrySet().stream().forEach(entry2 -> {
                Scene scene = entry2.getValue();
                scene.getBlocks().stream().forEach(block -> {
                    block.setId(UUID.randomUUID().toString());
                    switch (block.getType()) {
                        case GamePalConstants.BLOCK_TYPE_DROP:
                            WorldDrop worldDrop = new WorldDrop();
                            worldDrop.setType(block.getType());
                            worldDrop.setCode(block.getCode());
                            worldDrop.setId(block.getId());
                            worldDrop.setCoordinate(new Coordinate(block.getX(), block.getY()));
                            worldDrop.setSceneCoordinate(scene.getSceneCoordinate());
                            worldDrop.setRegionNo(region.getRegionNo());
                            worldDrop.setAmount(((Drop) block).getAmount());
                            worldDrop.setItemNo(((Drop) block).getItemNo());
                            world.getBlockMap().put(block.getId(), worldDrop);
                            break;
                        case GamePalConstants.BLOCK_TYPE_TELEPORT:
                            WorldTeleport worldTeleport = new WorldTeleport();
                            worldTeleport.setType(block.getType());
                            worldTeleport.setCode(block.getCode());
                            worldTeleport.setId(block.getId());
                            worldTeleport.setCoordinate(new Coordinate(block.getX(), block.getY()));
                            worldTeleport.setSceneCoordinate(scene.getSceneCoordinate());
                            worldTeleport.setRegionNo(region.getRegionNo());
                            worldTeleport.setTo(((Teleport) block).getTo());
                            world.getBlockMap().put(block.getId(), worldTeleport);
                            break;
                        case GamePalConstants.BLOCK_TYPE_GROUND:
                        case GamePalConstants.BLOCK_TYPE_WALL:
                        case GamePalConstants.BLOCK_TYPE_CEILING:
                        case GamePalConstants.BLOCK_TYPE_GROUND_DECORATION:
                        case GamePalConstants.BLOCK_TYPE_WALL_DECORATION:
                        case GamePalConstants.BLOCK_TYPE_CEILING_DECORATION:
                            WorldBlock regularWorldBlock = new WorldBlock();
                            regularWorldBlock.setType(block.getType());
                            regularWorldBlock.setCode(block.getCode());
                            regularWorldBlock.setId(block.getId());
                            regularWorldBlock.setCoordinate(new Coordinate(block.getX(), block.getY()));
                            regularWorldBlock.setSceneCoordinate(scene.getSceneCoordinate());
                            regularWorldBlock.setRegionNo(region.getRegionNo());
                            break;
                        default:
                            WorldBlock worldBlock = new WorldBlock();
                            worldBlock.setType(block.getType());
                            worldBlock.setCode(block.getCode());
                            worldBlock.setId(block.getId());
                            worldBlock.setCoordinate(new Coordinate(block.getX(), block.getY()));
                            worldBlock.setSceneCoordinate(scene.getSceneCoordinate());
                            worldBlock.setRegionNo(region.getRegionNo());
                            world.getBlockMap().put(block.getId(), worldBlock);
                            break;
                    }
                });
            });
        });
    }

    @Override
    public void loadItems() {
        JSONArray items = ContentUtil.jsonFile2JSONArray("src/main/resources/json/items.json");
        items.stream().forEach(itemObj -> {
            switch (((JSONObject) itemObj).getString("itemNo").charAt(0)) {
                case GamePalConstants.ITEM_CHARACTER_CONSUMABLE:
                    Consumable consumable = JSON.parseObject(String.valueOf(itemObj),Consumable.class);
                    itemMap.put(consumable.getItemNo(), consumable);
                    break;
                case GamePalConstants.ITEM_CHARACTER_JUNK:
                    Junk junk = JSON.parseObject(String.valueOf(itemObj),Junk.class);
                    itemMap.put(junk.getItemNo(), junk);
                    break;
                case GamePalConstants.ITEM_CHARACTER_TOOL:
                case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                case GamePalConstants.ITEM_CHARACTER_MATERIAL:
                case GamePalConstants.ITEM_CHARACTER_NOTE:
                case GamePalConstants.ITEM_CHARACTER_RECORDING:
                    Item item = JSON.parseObject(String.valueOf(itemObj),Item.class);
                    itemMap.put(item.getItemNo(), item);
                    break;
            }
        });
    }

    public void loadItemsOld() {
        JSONObject items = ContentUtil.jsonFile2JSONObject("src/main/resources/json/items.json");
        items.entrySet().stream().forEach(entry -> {
            switch (entry.getKey().charAt(0)) {
                case GamePalConstants.ITEM_CHARACTER_CONSUMABLE:
                    Consumable consumable = new Consumable();
                    consumable.setItemNo(entry.getKey());
                    consumable.setName(((JSONObject) entry.getValue()).getString("name"));
                    consumable.setWeight(((JSONObject) entry.getValue()).getBigDecimal("weight"));
                    consumable.setDescription(((JSONObject) entry.getValue()).getString("description"));
                    JSONObject effects = ((JSONObject) entry.getValue()).getJSONObject("effects");
                    effects.entrySet().stream().forEach(entry2 -> {
                        consumable.getEffects().put(String.valueOf(entry2.getKey()), (int) entry2.getValue());
                    });
                    itemMap.put(consumable.getItemNo(), consumable);
                    break;
                case GamePalConstants.ITEM_CHARACTER_JUNK:
                    Junk junk = new Junk();
                    junk.setItemNo(entry.getKey());
                    junk.setName(((JSONObject) entry.getValue()).getString("name"));
                    junk.setWeight(((JSONObject) entry.getValue()).getBigDecimal("weight"));
                    junk.setDescription(((JSONObject) entry.getValue()).getString("description"));
                    JSONObject materials = ((JSONObject) entry.getValue()).getJSONObject("materials");
                    materials.entrySet().stream().forEach(entry2 -> {
                        junk.getMaterials().put(String.valueOf(entry2.getKey()), (int) entry2.getValue());
                    });
                    itemMap.put(junk.getItemNo(), junk);
                    break;
                case GamePalConstants.ITEM_CHARACTER_TOOL:
                case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                case GamePalConstants.ITEM_CHARACTER_MATERIAL:
                case GamePalConstants.ITEM_CHARACTER_NOTE:
                case GamePalConstants.ITEM_CHARACTER_RECORDING:
                    Item item = new Item();
                    item.setItemNo(entry.getKey());
                    item.setName(((JSONObject) entry.getValue()).getString("name"));
                    item.setWeight(((JSONObject) entry.getValue()).getBigDecimal("weight"));
                    item.setDescription(((JSONObject) entry.getValue()).getString("description"));
                    itemMap.put(item.getItemNo(), item);
                    break;
            }
        });
    }

    @Override
    public void initiateGame(GameWorld world) {
        Game game = LasVegasGameUtil.getInstance();
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (!world.getGameMap().containsKey(i)) {
                world.getGameMap().put(i, game);
                break;
            }
        }
    }

    @Override
    public ResponseEntity addEvent(String userCode, WorldBlock eventBlock) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        WorldEvent event = new WorldEvent();
        PlayerUtil.copyWorldCoordinate(eventBlock, event);
        event.setUserCode(userCode);
        event.setCode(eventBlock.getType());
        event.setFrame(0);
        world.getEventQueue().add(event);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public void updateEvents(GameWorld world) {
        // Clear events from scene 24/02/16
        regionMap.entrySet().stream().forEach(region -> {
            region.getValue().getScenes().entrySet().forEach(scene -> {
                scene.getValue().setEvents(new ArrayList<>());
            });
        });
        Queue<WorldEvent> eventQueue = world.getEventQueue();
        WorldEvent tailEvent = new WorldEvent();
        eventQueue.add(tailEvent);
        while (tailEvent != eventQueue.peek()) {
            WorldEvent newEvent = PlayerUtil.updateEvent(eventQueue.poll());
            if (null != newEvent) {
                eventQueue.add(newEvent);
                if (!regionMap.containsKey(newEvent.getRegionNo())) {
                    logger.error(ErrorUtil.ERROR_1027);
                } else if (!regionMap.get(newEvent.getRegionNo()).getScenes().containsKey(newEvent.getSceneCoordinate())) {
                    logger.error(ErrorUtil.ERROR_1028);
                } else {
                    regionMap.get(newEvent.getRegionNo()).getScenes().get(newEvent.getSceneCoordinate()).getEvents()
                            .add(PlayerUtil.convertWorldEvent2Event(newEvent));
                }
            }
        }
        eventQueue.poll();
    }
}
