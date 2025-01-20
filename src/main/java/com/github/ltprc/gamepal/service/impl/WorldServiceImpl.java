package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.ItemConstants;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Do not put too many dynamic game logics into this class.
 */
@Transactional
@Service
public class WorldServiceImpl implements WorldService {

    private static final Log logger = LogFactory.getLog(WorldServiceImpl.class);
    private Map<String, GameWorld> worldMap = new LinkedHashMap<>(); // worldCode, world (We only allow 1 world now 24/02/16)
    private Map<String, Item> itemMap = new HashMap<>(); // itemNo, item
    private Map<String, Recipe> recipeMap = new HashMap<>(); // recipeNo, recipe

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private NpcManager npcManager;

    @Override
    public Map<String, GameWorld> getWorldMap() {
        return worldMap;
    }

    @Override
    public ResponseEntity<String> addWorld(String worldId) {
        JSONObject rst = ContentUtil.generateRst();
        if (worldMap.containsKey(worldId)) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1015));
        }
        GameWorld world = new GameWorld();
        initiateWorld(world);
        world.setId(worldId);
        worldMap.put(worldId, world);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public ResponseEntity<String> removeWorld(String worldId) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = worldMap.get(worldId);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1019));
        }
        world.getOnlineMap().entrySet().forEach(entry ->
            userService.logoff(entry.getKey().getId(), "", false)
        );
        worldMap.remove(worldId);
        return ResponseEntity.ok().body(rst.toString());
    }

    @Override
    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    @Override
    public Map<String, Recipe> getRecipeMap() {
        return recipeMap;
    }

    private void initiateWorld(GameWorld world) {
        Random random = new Random();
        world.setName("默认世界");
        world.setWorldTime(random.nextInt(GamePalConstants.MAX_WORLD_TIME));
        world.setWindDirection(BigDecimal.valueOf(random.nextDouble() * 360));
        world.setWindSpeed(BigDecimal.valueOf(random.nextDouble()).multiply(GamePalConstants.MAX_WIND_SPEED));
        world.setRegionMap(new ConcurrentHashMap<>());
        world.setInteractionInfoMap(new ConcurrentHashMap<>());
        world.setRelationMap(new ConcurrentHashMap<>());
        world.setSessionMap(new ConcurrentHashMap<>()); // userCode, session
        world.setTokenMap(new ConcurrentHashMap<>()); // userCode, token
        world.setOnlineMap(new ConcurrentHashMap<>());
        world.setGameMap(new ConcurrentHashMap<>());
        world.setMessageMap(new ConcurrentHashMap<>());
        world.setFlagMap(new ConcurrentHashMap<>());
        world.setTerminalMap(new ConcurrentHashMap<>());
        world.setNpcBrainMap(new ConcurrentHashMap<>());
        world.setBlockMap(new ConcurrentSkipListMap<>());
        world.setSourceMap(new ConcurrentHashMap<>());
        world.setCreatureMap(new ConcurrentHashMap<>());
        world.setPlayerInfoMap(new ConcurrentHashMap<>());
        world.setBagInfoMap(new ConcurrentHashMap<>());
        world.setPreservedBagInfoMap(new ConcurrentHashMap<>());
        world.setDropMap(new ConcurrentHashMap<>());
        world.setTeleportMap(new ConcurrentHashMap<>());
        world.setFarmMap(new ConcurrentHashMap<>());
        loadScenes(world);
    }

    private void loadScenes(GameWorld world) {
        JSONArray regions = ContentUtil.jsonFile2JSONArray("src/main/resources/config/regions.json");
        if (null == regions) {
            logger.error(ErrorUtil.ERROR_1032);
            return;
        }
        for (Object obj : regions) {
            JSONObject region = JSON.parseObject(String.valueOf(obj));
            int regionNo = region.getInteger("regionNo");
            int regionType = region.getInteger("type");
            String regionName =  region.getString("name");
            int height = region.getInteger("height");
            int width = region.getInteger("width");
            Region newRegion = new Region();
            newRegion.setRegionNo(regionNo);
            world.getRegionMap().put(regionNo, newRegion);
            newRegion.setType(regionType);
            newRegion.setName(regionName);
            newRegion.setHeight(height);
            newRegion.setWidth(width);
            newRegion.setRadius(GamePalConstants.REGION_RADIUS_DEFAULT);
            JSONArray scenes = region.getJSONArray("scenes");
            for (Object obj2 : scenes) {
                JSONObject scene = JSON.parseObject(String.valueOf(obj2));
                Scene newScene = new Scene();
                String name = scene.getString("name");
                int y = scene.getInteger("y");
                int x = scene.getInteger("x");
                newScene.setName(name);
                newScene.setSceneCoordinate(new IntegerCoordinate(x, y));
                newRegion.getScenes().put(newScene.getSceneCoordinate(), newScene);
                newScene.setBlocks(new ConcurrentHashMap<>());
                newScene.setGird(new int[newRegion.getWidth() + 1][newRegion.getHeight() + 1]);
                for (int i = 0; i <= newRegion.getWidth(); i++) {
                    for (int j = 0; j <= newRegion.getHeight(); j++) {
                        newScene.getGird()[i][j] = 1001;
                    }
                }
                // Collect normal square blocks from map object
                JSONArray map = scene.getJSONArray("map");
                if (null != map && !map.isEmpty()) {
                    for (int i = 0; i < Math.min(height, map.size()); i++) {
                        JSONArray blockRow = map.getJSONArray(i);
                        for (int j = 0; j < Math.min(width, blockRow.size()); j++) {
                            Integer value = blockRow.getInteger(j);
//                            int blockType = BlockConstants.BLOCK_TYPE_NORMAL;
//                            Structure structure;
//                            switch (value / 10000) {
//                                case 2:
//                                    structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
//                                            BlockConstants.STRUCTURE_LAYER_MIDDLE);
//                                    break;
//                                case 3:
//                                    structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
//                                            BlockConstants.STRUCTURE_LAYER_TOP);
//                                    break;
//                                case 1:
//                                default:
//                                    structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
//                                            BlockConstants.STRUCTURE_LAYER_BOTTOM);
//                                    break;
//                            }
                            WorldCoordinate worldCoordinate = new WorldCoordinate(newRegion.getRegionNo(),
                                    newScene.getSceneCoordinate(),
                                    new Coordinate(BigDecimal.valueOf(j), BigDecimal.valueOf(i)));
                            Block block = sceneManager.addLoadedBlock(world, String.valueOf(value % 10000), value / 10000, worldCoordinate);
//                            String id = UUID.randomUUID().toString();
//                            BlockInfo blockInfo = new BlockInfo(blockType, id, String.valueOf(value % 10000), structure);
//                            MovementInfo movementInfo = new MovementInfo();
//                            Block block = new Block(worldCoordinate, blockInfo, movementInfo);
//                            newScene.getBlocks().add(block);
//                            if (BlockUtil.checkBlockTypeInteractive(block.getBlockInfo().getType())) {
//                                world.getBlockMap().put(id, block);
//                            }
                        }
                    }
                }
                // Collect special blocks from blocks object
                JSONArray blocks = scene.getJSONArray("blocks");
                if (null != blocks && !blocks.isEmpty()) {
                    for (int i = 0; i < blocks.size(); i++) {
                        JSONArray blockRow = blocks.getJSONArray(i);
                        Integer type = blockRow.getInteger(0);
                        WorldCoordinate worldCoordinate = new WorldCoordinate(newRegion.getRegionNo(),
                                newScene.getSceneCoordinate(),
                                new Coordinate(BigDecimal.valueOf(blockRow.getInteger(2)),
                                        BigDecimal.valueOf(blockRow.getInteger(3))));
                        Block block;
                        switch (type) {
                            case BlockConstants.BLOCK_TYPE_DROP:
                                block = sceneManager.addDropBlock(world, worldCoordinate, new AbstractMap.SimpleEntry<>(blockRow.getString(4), blockRow.getInteger(5)));
                                break;
                            case BlockConstants.BLOCK_TYPE_TELEPORT:
                                block = sceneManager.addTeleportBlock(world, String.valueOf(blockRow.getInteger(1)), worldCoordinate, new WorldCoordinate(blockRow.getInteger(4),
                                        new IntegerCoordinate(blockRow.getInteger(5),
                                                blockRow.getInteger(6)),
                                        new Coordinate(BigDecimal.valueOf(blockRow.getInteger(7)),
                                                BigDecimal.valueOf(blockRow.getInteger(8)))));
                                break;
                            default:
                                BlockInfo blockInfo1 = BlockUtil.createBlockInfoByType(type);
                                if (null != blockInfo1) {
                                    blockInfo1.setCode(String.valueOf(blockRow.getInteger(1)));
                                    block = sceneManager.addOtherBlock(world, worldCoordinate, blockInfo1, new MovementInfo());
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void loadItems() {
        JSONArray items = ContentUtil.jsonFile2JSONArray("src/main/resources/config/items.json");
        if (null == items) {
            logger.error(ErrorUtil.ERROR_1032);
            return;
        }
        items.forEach(itemObj -> {
            switch (((JSONObject) itemObj).getString("itemNo").charAt(0)) {
                case ItemConstants.ITEM_CHARACTER_TOOL:
                    Tool tool = JSON.parseObject(String.valueOf(itemObj), Tool.class);
//                    SkillUtil.defineToolProps(tool);
                    itemMap.put(tool.getItemNo(), tool);
                    break;
                case ItemConstants.ITEM_CHARACTER_OUTFIT:
                    Outfit outfit = JSON.parseObject(String.valueOf(itemObj), Outfit.class);
                    itemMap.put(outfit.getItemNo(), outfit);
                    break;
                case ItemConstants.ITEM_CHARACTER_CONSUMABLE:
                    Consumable consumable = JSON.parseObject(String.valueOf(itemObj), Consumable.class);
                    itemMap.put(consumable.getItemNo(), consumable);
                    break;
                case ItemConstants.ITEM_CHARACTER_JUNK:
                    Junk junk = JSON.parseObject(String.valueOf(itemObj), Junk.class);
                    itemMap.put(junk.getItemNo(), junk);
                    break;
                case ItemConstants.ITEM_CHARACTER_MATERIAL:
                case ItemConstants.ITEM_CHARACTER_AMMO:
                case ItemConstants.ITEM_CHARACTER_NOTE:
                case ItemConstants.ITEM_CHARACTER_RECORDING:
                default:
                    Item item = JSON.parseObject(String.valueOf(itemObj), Item.class);
                    itemMap.put(item.getItemNo(), item);
                    break;
            }
        });
    }

    @Override
    public void loadRecipes() {
        JSONArray recipes = ContentUtil.jsonFile2JSONArray("src/main/resources/config/recipes.json");
        if (null == recipes) {
            logger.error(ErrorUtil.ERROR_1032);
            return;
        }
        recipes.stream()
                .map(recipeObj -> ((JSONObject) recipeObj).toJavaObject(Recipe.class))
                .sorted((recipe1, recipe2) -> StringUtils.compare(recipe1.getRecipeNo(), recipe2.getRecipeNo()))
                .forEach(recipe -> recipeMap.put(recipe.getRecipeNo(), recipe));
    }

    @Override
    public void expandByCoordinate(GameWorld world, WorldCoordinate fromWorldCoordinate,
                                   WorldCoordinate toWorldCoordinate, int depth) {
        boolean isRegionChanged = null == fromWorldCoordinate
                || fromWorldCoordinate.getRegionNo() != toWorldCoordinate.getRegionNo();
        if (isRegionChanged) {
            expandRegion(world, toWorldCoordinate.getRegionNo());
        }
        boolean isSceneChanged = isRegionChanged
                || !fromWorldCoordinate.getSceneCoordinate().getX()
                .equals(toWorldCoordinate.getSceneCoordinate().getX())
                || !fromWorldCoordinate.getSceneCoordinate().getY()
                .equals(toWorldCoordinate.getSceneCoordinate().getY());
        if (isSceneChanged) {
            expandScene(world, toWorldCoordinate, depth);
        }
    }

    @Override
    public void expandRegion(GameWorld world, int regionNo) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        if (!regionMap.containsKey(regionNo)) {
            Region region = sceneManager.generateRegion(regionNo);
            world.getRegionMap().put(regionNo, region);
        }
    }

    @Override
    public void expandScene(GameWorld world, WorldCoordinate worldCoordinate, int depth) {
        if (depth < 0) {
            return;
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        WorldCoordinate newWorldCoordinate = new WorldCoordinate(worldCoordinate);
        for (int i = - depth; i <= depth; i++) {
            for (int j = - depth; j <= depth; j++) {
                if (worldCoordinate.getSceneCoordinate().getX() + i >= - region.getRadius()
                        && worldCoordinate.getSceneCoordinate().getX() + i <= region.getRadius()
                        && worldCoordinate.getSceneCoordinate().getY() + j >= - region.getRadius()
                        && worldCoordinate.getSceneCoordinate().getY() + j <= region.getRadius()) {
                    newWorldCoordinate.setSceneCoordinate(new IntegerCoordinate(
                            worldCoordinate.getSceneCoordinate().getX() + i,
                            worldCoordinate.getSceneCoordinate().getY() + j));
                    sceneManager.fillScene(world, region, newWorldCoordinate.getSceneCoordinate());
                }
            }
        }
    }

    @Override
    public void updateWorldTime(GameWorld world, int increment) {
        world.setWorldTime((world.getWorldTime() + increment) % GamePalConstants.MAX_WORLD_TIME);
    }

    @Override
    public void registerOnline(GameWorld world, BlockInfo blockInfo) {
        long timestamp = Instant.now().getEpochSecond();
        world.getOnlineMap().put(blockInfo, timestamp);
    }

    @Override
    public void registerOffline(GameWorld world, BlockInfo blockInfo) {
        world.getOnlineMap().remove(blockInfo);
    }
}
