package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import com.github.ltprc.gamepal.util.lv.LasVegasGameUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
        GameWorld world = worldMap.get(worldCode);
        if (null == world) {
            return ResponseEntity.ok().body(JSON.toJSONString(ErrorUtil.ERROR_1019));
        }
        world.getOnlineMap().entrySet().forEach(entry ->
            userService.logoff(entry.getKey(), "", false)
        );
        worldMap.remove(worldCode);
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
        world.setWorldTime(random.nextInt(GamePalConstants.MAX_WORLD_TIME));
        world.setWindDirection(BigDecimal.valueOf(random.nextDouble() * 360));
        world.setWindSpeed(BigDecimal.valueOf(random.nextDouble()));
        world.setRegionMap(new ConcurrentHashMap<>());
        world.setPlayerInfoMap(new ConcurrentHashMap<>());
        world.setRelationMap(new ConcurrentHashMap<>());
        world.setSessionMap(new ConcurrentHashMap<>()); // userCode, session
        world.setTokenMap(new ConcurrentHashMap<>()); // userCode, token
        world.setOnlineMap(new ConcurrentHashMap<>()); // userCode, timestamp
        world.setBlockMap(new ConcurrentSkipListMap<>());
        world.setGameMap(new ConcurrentHashMap<>());
        world.setEventQueue(new ConcurrentLinkedQueue<>());
        world.setMessageMap(new ConcurrentHashMap<>());
        world.setFlagMap(new ConcurrentHashMap<>());
        world.setTerminalMap(new ConcurrentHashMap<>());
        world.setNpcBrainMap(new ConcurrentHashMap<>());
        loadScenes(world);
        registerInteractiveBlocks(world);
        initiateGame(world);
    }

    private void loadScenes(GameWorld world) {
        JSONArray regions = ContentUtil.jsonFile2JSONArray("src/main/resources/json/regions.json");
        if (null == regions) {
            logger.error(ErrorUtil.ERROR_1032);
            return;
        }
        for (Object obj : regions) {
            JSONObject region = JSON.parseObject(String.valueOf(obj));
            Region newRegion = new Region();
            int regionNo = region.getInteger("regionNo");
            String regionName =  region.getString("name");
            int height = region.getInteger("height");
            int width = region.getInteger("width");
            newRegion.setRegionNo(regionNo);
            newRegion.setName(regionName);
            newRegion.setHeight(height);
            newRegion.setWidth(width);
            newRegion.setRadius(GamePalConstants.SCENE_SCAN_MAX_RADIUS);
            newRegion.setScenes(new HashMap<>());
            newRegion.setTerrainMap(new HashMap<>());
            newRegion.setAltitudeMap(new HashMap<>());
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
                newScene.setEvents(new CopyOnWriteArrayList<>());
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
                            int blockType = GamePalConstants.BLOCK_TYPE_NORMAL;
                            Structure structure;
                            switch (value / 10000) {
                                case 2:
                                    structure = new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                            GamePalConstants.STRUCTURE_LAYER_MIDDLE);
                                    break;
                                case 3:
                                    structure = new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                            GamePalConstants.STRUCTURE_LAYER_TOP);
                                    break;
                                case 1:
                                default:
                                    structure = new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                            GamePalConstants.STRUCTURE_LAYER_BOTTOM);
                                    break;
                            }
                            Block block = new Block(blockType, null, String.valueOf(value % 10000), structure,
                                    new Coordinate(BigDecimal.valueOf(j), BigDecimal.valueOf(i)));
                            newScene.getBlocks().add(block);
                        }
                    }
                }
                // Collect special blocks from blocks object
                JSONArray blocks = scene.getJSONArray("blocks");
                if (null != blocks && !blocks.isEmpty()) {
                    for (int i = 0; i < blocks.size(); i++) {
                        JSONArray blockRow = blocks.getJSONArray(i);
                        Integer type = blockRow.getInteger(0);
                        Block block = new Block(type, null, String.valueOf(blockRow.getInteger(1)),
                                new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                        GamePalConstants.STRUCTURE_LAYER_MIDDLE_DECORATION),
                                        new Coordinate(BigDecimal.valueOf(blockRow.getInteger(2)),
                                                BigDecimal.valueOf(blockRow.getInteger(3))));
                        switch (type) {
                            case GamePalConstants.BLOCK_TYPE_DROP:
                                block.getStructure().setMaterial(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW);
                                block.getStructure().setLayer(GamePalConstants.STRUCTURE_LAYER_MIDDLE);
                                Drop drop = new Drop(blockRow.getString(4), blockRow.getInteger(5), block);
                                newScene.getBlocks().add(drop);
                                break;
                            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                                WorldCoordinate to = new WorldCoordinate(blockRow.getInteger(4),
                                        new IntegerCoordinate(blockRow.getInteger(5),
                                                blockRow.getInteger(6)),
                                        new Coordinate(BigDecimal.valueOf(blockRow.getInteger(7)),
                                                BigDecimal.valueOf(blockRow.getInteger(8))));
                                block.getStructure().setMaterial(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW);
                                block.getStructure().setLayer(GamePalConstants.STRUCTURE_LAYER_BOTTOM_DECORATION);
                                Teleport teleport = new Teleport(to, block);
                                newScene.getBlocks().add(teleport);
                                break;
                            default:
                                newScene.getBlocks().add(block);
                                break;
                        }
                    }
                }
                newRegion.getScenes().put(newScene.getSceneCoordinate(), newScene);
            }
            world.getRegionMap().put(regionNo, newRegion);
        }
    }

    private void registerInteractiveBlocks(GameWorld world) {
        world.getRegionMap().entrySet().forEach(entry1 -> {
            Region region = entry1.getValue();
            region.getScenes().entrySet().forEach(entry2 -> {
                Scene scene = entry2.getValue();
                scene.getBlocks().forEach(block -> {
                    block.setId(UUID.randomUUID().toString());
                    WorldBlock worldBlock = BlockUtil.convertBlock2WorldBlock(block, region.getRegionNo(),
                            scene.getSceneCoordinate(), block);
                    switch (block.getType()) {
                        case GamePalConstants.BLOCK_TYPE_DROP:
                        case GamePalConstants.BLOCK_TYPE_TELEPORT:
                            world.getBlockMap().put(block.getId(), worldBlock);
                            break;
                        default:
                            if (BlockUtil.checkBlockTypeInteractive(block.getType())) {
                                world.getBlockMap().put(block.getId(), worldBlock);
                            }
                            break;
                    }
                });
            });
        });
    }

    @Override
    public void loadItems() {
        JSONArray items = ContentUtil.jsonFile2JSONArray("src/main/resources/json/items.json");
        if (null == items) {
            logger.error(ErrorUtil.ERROR_1032);
            return;
        }
        items.forEach(itemObj -> {
            switch (((JSONObject) itemObj).getString("itemNo").charAt(0)) {
                case GamePalConstants.ITEM_CHARACTER_TOOL:
                    Tool tool = JSON.parseObject(String.valueOf(itemObj), Tool.class);
                    SkillUtil.defineToolProps(tool);
                    itemMap.put(tool.getItemNo(), tool);
                    break;
                case GamePalConstants.ITEM_CHARACTER_OUTFIT:
                    Outfit outfit = JSON.parseObject(String.valueOf(itemObj), Outfit.class);
                    itemMap.put(outfit.getItemNo(), outfit);
                    break;
                case GamePalConstants.ITEM_CHARACTER_CONSUMABLE:
                    Consumable consumable = JSON.parseObject(String.valueOf(itemObj), Consumable.class);
                    itemMap.put(consumable.getItemNo(), consumable);
                    break;
                case GamePalConstants.ITEM_CHARACTER_JUNK:
                    Junk junk = JSON.parseObject(String.valueOf(itemObj), Junk.class);
                    itemMap.put(junk.getItemNo(), junk);
                    break;
                case GamePalConstants.ITEM_CHARACTER_MATERIAL:
                case GamePalConstants.ITEM_CHARACTER_NOTE:
                case GamePalConstants.ITEM_CHARACTER_RECORDING:
                default:
                    Item item = JSON.parseObject(String.valueOf(itemObj), Item.class);
                    itemMap.put(item.getItemNo(), item);
                    break;
            }
        });
    }

    @Override
    public void loadRecipes() {
        JSONArray recipes = ContentUtil.jsonFile2JSONArray("src/main/resources/json/recipes.json");
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
    public ResponseEntity<String> addEvent(String userCode, WorldBlock eventBlock) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        List<WorldBlock> playerInfoList = world.getPlayerInfoMap().values().stream()
                .filter(playerInfo -> checkEventCondition(eventBlock, playerInfo))
                .collect(Collectors.toList());
        activateEvent(eventBlock, playerInfoList);
        return ResponseEntity.ok().body(rst.toString());
    }

    private boolean checkEventCondition(final WorldBlock eventBlock, final WorldBlock blocker) {
        return eventBlock.getRegionNo() == blocker.getRegionNo()
                && (blocker.getType() != GamePalConstants.BLOCK_TYPE_PLAYER
                || SkillUtil.validateDamage((PlayerInfo) blocker))
                && checkEventConditionByEventCode(eventBlock, blocker);
    }

    private boolean checkEventConditionByEventCode(final WorldBlock eventBlock, final WorldBlock blocker) {
        GameWorld world = userService.getWorldByUserCode(eventBlock.getId());
        Map<Integer, Region> regionMap = world.getRegionMap();
        PlayerInfo fromPlayerInfo = world.getPlayerInfoMap().get(eventBlock.getId());
        boolean rst = true;
        switch (Integer.valueOf(eventBlock.getCode())) {
            case GamePalConstants.EVENT_CODE_FIRE:
                rst = BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), eventBlock, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_FIRE) <= 0;
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
                double angle1 = BlockUtil.calculateAngle(regionMap.get(eventBlock.getRegionNo()), fromPlayerInfo,
                        blocker).doubleValue();
                double angle2 = fromPlayerInfo.getFaceDirection().doubleValue();
                double deltaAngle = BlockUtil.compareAnglesInDegrees(angle1, angle2);
                rst = !eventBlock.getId().equals(blocker.getId())
                        && BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), fromPlayerInfo, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_MELEE) <= 0
                        && deltaAngle < GamePalConstants.EVENT_MAX_ANGLE_MELEE.doubleValue();
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                BigDecimal shakingAngle = BigDecimal.valueOf(Math.random() * 2 - 1);
                rst = !eventBlock.getId().equals(blocker.getId())
                        && BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), fromPlayerInfo, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_SHOOT) <= 0
                        && BlockUtil.detectLineSquareCollision(regionMap.get(eventBlock.getRegionNo()),
                        fromPlayerInfo, fromPlayerInfo.getFaceDirection().add(shakingAngle), blocker)
                        && BlockUtil.compareAnglesInDegrees(
                        BlockUtil.calculateAngle(regionMap.get(eventBlock.getRegionNo()), fromPlayerInfo,
                                blocker).doubleValue(), fromPlayerInfo.getFaceDirection().doubleValue()) < 135D;
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                rst = BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), eventBlock, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_EXPLODE) <= 0;
                break;
            default:
                break;
        }
        return rst;
    }

    private void activateEvent(final WorldBlock eventBlock, final List<WorldBlock> playerInfoList) {
        Random random = new Random();
        GameWorld world = userService.getWorldByUserCode(eventBlock.getId());
        Map<Integer, Region> regionMap = world.getRegionMap();
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        WorldEvent worldEvent;
        switch (Integer.valueOf(eventBlock.getCode())) {
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_CURSE:
            case GamePalConstants.EVENT_CODE_CHEER:
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()), eventBlock);
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_HEAL:
                playerService.changeHp(eventBlock.getId(), GamePalConstants.EVENT_HEAL_HEAL, false);
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()), eventBlock);
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_FIRE:
                playerInfoList.stream().forEach(playerInfo ->
                    playerService.damageHp(playerInfo.getId(), eventBlock.getId(),
                            -GamePalConstants.EVENT_DAMAGE_PER_FRAME_FIRE, false));
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()), eventBlock);
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
                playerInfoList.stream().forEach(playerInfo -> {
                    int damageValue = -GamePalConstants.EVENT_DAMAGE_MELEE;
                    if (userService.getWorldByUserCode(playerInfo.getId()).getEventQueue().stream()
                            .anyMatch(event -> GamePalConstants.EVENT_CODE_BLOCK == event.getCode()
                                    && playerInfo.getId().equals(event.getUserCode()))) {
                        // This player is blocking 24/03/15
                        damageValue /= 2;
                    }
                    playerService.damageHp(playerInfo.getId(), eventBlock.getId(), damageValue, false);
                    WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(playerInfo.getRegionNo()), playerInfo,
                            BigDecimal.valueOf(random.nextDouble() * 360), BigDecimal.valueOf(random.nextDouble() / 2));
                    WorldEvent worldEvent1 = BlockUtil.createWorldEvent(playerInfo.getId(),
                            GamePalConstants.EVENT_CODE_BLEED, bleedWc);
                    world.getEventQueue().add(worldEvent1);
                });
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()), eventBlock);
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                List<WorldBlock> activatedWorldBlockList = calculateBallisticWorldBlocks(eventBlock, playerInfoList);
                activatedWorldBlockList.stream().forEach((WorldBlock activatedWorldBlock) -> {
                    BlockUtil.copyWorldCoordinate(activatedWorldBlock, eventBlock);
                    if (activatedWorldBlock.getType() == GamePalConstants.BLOCK_TYPE_PLAYER
                            && playerInfoMap.containsKey(activatedWorldBlock.getId())
                            && SkillUtil.validateDamage(playerInfoMap.get(activatedWorldBlock.getId()))) {
                        playerService.damageHp(activatedWorldBlock.getId(), eventBlock.getId(),
                                -GamePalConstants.EVENT_DAMAGE_SHOOT, false);
                        WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                world.getRegionMap().get(activatedWorldBlock.getRegionNo()), activatedWorldBlock,
                                BigDecimal.valueOf(random.nextDouble() * 360),
                                BigDecimal.valueOf(random.nextDouble() / 2));
                        WorldEvent worldEvent1 = BlockUtil.createWorldEvent(activatedWorldBlock.getId(),
                                GamePalConstants.EVENT_CODE_BLEED, bleedWc);
                        world.getEventQueue().add(worldEvent1);
                    }
                });
                // Shake the final position of event 24/04/05
                WorldCoordinate shakenWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        regionMap.get(eventBlock.getRegionNo()), eventBlock,
                        BigDecimal.valueOf(random.nextDouble() * 360), BigDecimal.valueOf(random.nextDouble() / 2));
                BlockUtil.copyWorldCoordinate(shakenWc, eventBlock);
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()),
                        eventBlock);
                world.getEventQueue().add(worldEvent);
                if (Integer.valueOf(eventBlock.getCode()) == GamePalConstants.EVENT_CODE_SHOOT_ROCKET) {
                    addEvent(eventBlock.getId(), BlockUtil.convertEvent2WorldBlock(
                            world.getRegionMap().get(eventBlock.getRegionNo()), eventBlock.getId(),
                            GamePalConstants.EVENT_CODE_EXPLODE, eventBlock));
                    // Add tail smoke 24/03/16
                    BigDecimal tailSmokeLength = BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()),
                            world.getPlayerInfoMap().get(eventBlock.getId()), eventBlock);
                    int tailSmokeAmount = tailSmokeLength.intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(eventBlock.getRegionNo()), world.getPlayerInfoMap().get(eventBlock.getId()),
                            eventBlock, tailSmokeAmount);
                    equidistantPoints.stream()
                            .forEach(tailSmokeCoordinate -> {
                                BlockUtil.fixWorldCoordinate(regionMap.get(eventBlock.getRegionNo()), tailSmokeCoordinate);
                                WorldEvent worldEvent1 = BlockUtil.createWorldEvent(eventBlock.getId(),
                                        GamePalConstants.EVENT_CODE_TAIL_SMOKE, tailSmokeCoordinate);
                                world.getEventQueue().add(worldEvent1);
                            });
                }
                if (Integer.valueOf(eventBlock.getCode()) == GamePalConstants.EVENT_CODE_SHOOT_SLUG
                        || Integer.valueOf(eventBlock.getCode()) == GamePalConstants.EVENT_CODE_SHOOT_MAGNUM
                        || Integer.valueOf(eventBlock.getCode()) == GamePalConstants.EVENT_CODE_SHOOT_ROCKET) {
                    PlayerInfo fromPlayerInfo = world.getPlayerInfoMap().get(eventBlock.getId());
                    WorldCoordinate sparkWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(fromPlayerInfo.getRegionNo()), fromPlayerInfo,
                            fromPlayerInfo.getFaceDirection().add(BigDecimal.valueOf(
                                    GamePalConstants.EVENT_MAX_ANGLE_SHOOT.doubleValue() * 2
                                            * (random.nextDouble() - 0.5D))), BigDecimal.ONE);
                    worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), GamePalConstants.EVENT_CODE_SPARK, sparkWc);
                    world.getEventQueue().add(worldEvent);
                }
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                playerInfoList.stream().forEach(playerInfo -> {
                    playerService.damageHp(playerInfo.getId(), eventBlock.getId(),
                            -GamePalConstants.EVENT_DAMAGE_EXPLODE, false);
                    WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(playerInfo.getRegionNo()), playerInfo,
                            BigDecimal.valueOf(random.nextDouble() * 360), BigDecimal.valueOf(random.nextDouble() / 2));
                    WorldEvent worldEvent1 = BlockUtil.createWorldEvent(playerInfo.getId(),
                            GamePalConstants.EVENT_CODE_BLEED, bleedWc);
                    world.getEventQueue().add(worldEvent1);
                });
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()), eventBlock);
                world.getEventQueue().add(worldEvent);
                break;
            default:
                break;
        }
    }

    private List<WorldBlock> calculateBallisticWorldBlocks(final WorldBlock eventBlock, final List<WorldBlock> playerInfoList) {
        GameWorld world = userService.getWorldByUserCode(eventBlock.getId());
        PlayerInfo eventPlayerInfo = world.getPlayerInfoMap().get(eventBlock.getId());
        Map<Integer, Region> regionMap = world.getRegionMap();
        List<WorldBlock> activatedWorldBlockList = new ArrayList<>();
        activatedWorldBlockList.add(new WorldBlock(eventBlock));
        // Detect the nearest blocker (including playerInfos) 24/03/21
        List<IntegerCoordinate> preSelectedSceneCoordinates = BlockUtil.preSelectSceneCoordinates(
                regionMap.get(eventBlock.getRegionNo()), eventPlayerInfo, eventBlock);
        List<WorldBlock> preSelectedWorldBlocks = new ArrayList<>(playerInfoList);
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
            regionMap.get(eventBlock.getRegionNo()).getScenes().get(sceneCoordinate).getBlocks().stream()
                    .filter(blocker ->
                            GamePalConstants.STRUCTURE_MATERIAL_HOLLOW != blocker.getStructure().getMaterial())
                    .forEach(blocker -> {
                        WorldBlock worldBlock = BlockUtil.convertBlock2WorldBlock(blocker, eventBlock.getRegionNo(),
                                sceneCoordinate, blocker);
                        preSelectedWorldBlocks.add(worldBlock);
                    })
        );
        preSelectedWorldBlocks.stream()
                .filter(wb -> checkEventCondition(eventBlock, wb))
                .forEach(wb -> {
                    BigDecimal distanceOld =
                            BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()),
                                    eventPlayerInfo, activatedWorldBlockList.get(activatedWorldBlockList.size() - 1));
                    BigDecimal distanceNew =
                            BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()),
                                    eventPlayerInfo, wb);
                    if (null != distanceOld && null != distanceNew && distanceOld.compareTo(distanceNew) > 0) {
                        if (wb.getType() != GamePalConstants.BLOCK_TYPE_PLAYER
                                || eventBlock.getType() != SkillConstants.SKILL_CODE_SHOOT_MAGNUM) {
                            activatedWorldBlockList.clear();
                        }
                        activatedWorldBlockList.add(new WorldBlock(wb));
                    }
                });
        return activatedWorldBlockList;
    }

    @Override
    public void updateEvents(GameWorld world) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        // Clear events from scene 24/02/16
        regionMap.entrySet().stream().forEach(region ->
            region.getValue().getScenes().entrySet().forEach(scene ->
                scene.getValue().setEvents(new CopyOnWriteArrayList<>())
            )
        );
        Queue<WorldEvent> eventQueue = world.getEventQueue();
        WorldEvent tailEvent = new WorldEvent();
        eventQueue.add(tailEvent);
        while (tailEvent != eventQueue.peek()) {
            WorldEvent newEvent = updateEvent(eventQueue.poll());
            if (null != newEvent) {
                eventQueue.add(newEvent);
                if (!regionMap.containsKey(newEvent.getRegionNo())) {
                    logger.error(ErrorUtil.ERROR_1027);
                } else {
                    if (!regionMap.get(newEvent.getRegionNo()).getScenes().containsKey(newEvent.getSceneCoordinate())) {
                        // Detect and expand scenes after updating event's location
                        expandScene(world, newEvent);
                    }
                    regionMap.get(newEvent.getRegionNo()).getScenes().get(newEvent.getSceneCoordinate()).getEvents()
                            .add(BlockUtil.convertWorldEvent2Event(newEvent));
                }
            }
        }
        eventQueue.poll();
    }

    @Override
    public void expandScene(GameWorld world, WorldCoordinate worldCoordinate) {
        expandScene(world, worldCoordinate, GamePalConstants.SCENE_SCAN_RADIUS);
    }

    private void expandScene(GameWorld world, WorldCoordinate worldCoordinate, int depth) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region;
        if (!regionMap.containsKey(worldCoordinate.getRegionNo())) {
            region = sceneManager.generateRegion(worldCoordinate.getRegionNo());
            regionMap.put(worldCoordinate.getRegionNo(), region);
        }
        region = regionMap.get(worldCoordinate.getRegionNo());
        if (null == region.getScenes()) {
            region.setScenes(new HashMap<>());
        }
        if (!region.getScenes().containsKey(worldCoordinate.getSceneCoordinate())) {
            sceneManager.fillScene(world, region, worldCoordinate.getSceneCoordinate());
        }
        if (depth > 0) {
            WorldCoordinate newWorldCoordinate = new WorldCoordinate();
            BlockUtil.copyWorldCoordinate(worldCoordinate, newWorldCoordinate);
            for (int i = - 1; i <= 1; i++) {
                for (int j = - 1; j <= 1; j++) {
                    if (worldCoordinate.getSceneCoordinate().getX() + i >= - region.getRadius()
                            && worldCoordinate.getSceneCoordinate().getX() + i <= region.getRadius()
                            && worldCoordinate.getSceneCoordinate().getY() + j >= - region.getRadius()
                            && worldCoordinate.getSceneCoordinate().getY() + j <= region.getRadius()) {
                        newWorldCoordinate.setSceneCoordinate(new IntegerCoordinate(
                                worldCoordinate.getSceneCoordinate().getX() + i,
                                worldCoordinate.getSceneCoordinate().getY() + j));
                        expandScene(world, newWorldCoordinate, depth - 1);
                    }
                }
            }
        }
    }

    private WorldEvent updateEvent(WorldEvent oldEvent) {
        GameWorld world = userService.getWorldByUserCode(oldEvent.getUserCode());
//        triggerEvent(oldEvent); // Deleted 24/04/05
        WorldEvent newEvent = new WorldEvent();
        newEvent.setUserCode(oldEvent.getUserCode());
        newEvent.setCode(oldEvent.getCode());
        // Set location 24/03/05
        switch (oldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_SACRIFICE:
            case GamePalConstants.EVENT_CODE_DISTURB:
                // Stick with playerInfo
                BlockUtil.copyWorldCoordinate(world.getPlayerInfoMap().get(oldEvent.getUserCode()), newEvent);
                break;
            case GamePalConstants.EVENT_CODE_CHEER:
            case GamePalConstants.EVENT_CODE_CURSE:
                // Stick with playerInfo above
                BlockUtil.copyWorldCoordinate(world.getPlayerInfoMap().get(oldEvent.getUserCode()), newEvent);
                newEvent.getCoordinate().setY(newEvent.getCoordinate().getY().subtract(BigDecimal.ONE));
                BlockUtil.fixWorldCoordinate(world.getRegionMap().get(newEvent.getRegionNo()), newEvent);
                break;
            default:
                // Keep its position
                BlockUtil.copyWorldCoordinate(oldEvent, newEvent);
                break;
        }
        newEvent.setFrame(oldEvent.getFrame() + 1);
        newEvent.setFrameMax(oldEvent.getFrameMax());
        newEvent.setPeriod(oldEvent.getPeriod());
        if (newEvent.getFrame() >= newEvent.getPeriod()) {
            if (newEvent.getFrameMax() == -1) {
                newEvent.setFrame(newEvent.getFrame() - newEvent.getPeriod());
            } else {
                return null;
            }
        }
        return newEvent;
    }

    @Override
    public void updateWorldTime(GameWorld world, int increment) {
        world.setWorldTime((world.getWorldTime() + increment) % GamePalConstants.MAX_WORLD_TIME);
    }
}
