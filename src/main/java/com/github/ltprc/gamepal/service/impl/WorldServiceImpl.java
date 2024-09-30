package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.ItemConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ContentUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
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
            userService.logoff(entry.getKey(), "", false)
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
        world.setWindSpeed(BigDecimal.valueOf(random.nextDouble()));
        world.setRegionMap(new ConcurrentHashMap<>());
        world.setInteractionInfoMap(new ConcurrentHashMap<>());
        world.setRelationMap(new ConcurrentHashMap<>());
        world.setSessionMap(new ConcurrentHashMap<>()); // userCode, session
        world.setTokenMap(new ConcurrentHashMap<>()); // userCode, token
        world.setOnlineMap(new ConcurrentHashMap<>()); // userCode, timestamp
        world.setGameMap(new ConcurrentHashMap<>());
        world.setEventQueue(new ConcurrentLinkedQueue<>());
        world.setMessageMap(new ConcurrentHashMap<>());
        world.setFlagMap(new ConcurrentHashMap<>());
        world.setTerminalMap(new ConcurrentHashMap<>());
        world.setNpcBrainMap(new ConcurrentHashMap<>());
        world.setBlockMap(new ConcurrentSkipListMap<>());
//        world.setPlayerInfoMap(new ConcurrentHashMap<>());
        world.setCreatureMap(new ConcurrentHashMap<>());
        world.setBagInfoMap(new ConcurrentHashMap<>());
        world.setPreservedBagInfoMap(new ConcurrentHashMap<>());
        world.setDropMap(new ConcurrentHashMap<>());
        world.setTeleportMap(new ConcurrentHashMap<>());
        loadScenes(world);
//        registerInteractiveBlocks(world);
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
                newScene.setBlocks(new CopyOnWriteArrayList<>());
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
                            int blockType = BlockConstants.BLOCK_TYPE_NORMAL;
                            Structure structure;
                            switch (value / 10000) {
                                case 2:
                                    structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                            BlockConstants.STRUCTURE_LAYER_MIDDLE);
                                    break;
                                case 3:
                                    structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                            BlockConstants.STRUCTURE_LAYER_TOP);
                                    break;
                                case 1:
                                default:
                                    structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_HOLLOW,
                                            BlockConstants.STRUCTURE_LAYER_BOTTOM);
                                    break;
                            }
                            WorldCoordinate worldCoordinate = new WorldCoordinate(newRegion.getRegionNo(),
                                    newScene.getSceneCoordinate(),
                                    new Coordinate(BigDecimal.valueOf(j), BigDecimal.valueOf(i)));
                            String id = UUID.randomUUID().toString();
                            BlockInfo blockInfo = new BlockInfo(blockType, id, String.valueOf(value % 10000), structure);
                            MovementInfo movementInfo = new MovementInfo();
                            Block block = new Block(worldCoordinate, blockInfo, movementInfo);
                            newScene.getBlocks().add(block);
                            if (BlockUtil.checkBlockTypeInteractive(block.getBlockInfo().getType())) {
                                world.getBlockMap().put(id, block);
                            }
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
                        String id = UUID.randomUUID().toString();
                        BlockInfo blockInfo = new BlockInfo(type, id, String.valueOf(blockRow.getInteger(1)),
                                new Structure(BlockConstants.STRUCTURE_MATERIAL_SOLID,
                                        BlockConstants.STRUCTURE_LAYER_MIDDLE_DECORATION));
                        MovementInfo movementInfo = new MovementInfo();
                        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
                        newScene.getBlocks().add(block);
                        if (BlockUtil.checkBlockTypeInteractive(block.getBlockInfo().getType())) {
                            world.getBlockMap().put(id, block);
                        }
                        switch (type) {
                            case BlockConstants.BLOCK_TYPE_DROP:
                                block.getBlockInfo().getStructure().setMaterial(BlockConstants.STRUCTURE_MATERIAL_HOLLOW);
                                block.getBlockInfo().getStructure().setLayer(BlockConstants.STRUCTURE_LAYER_MIDDLE);
                                world.getDropMap().put(block.getBlockInfo().getId(),
                                        new AbstractMap.SimpleEntry<>(blockRow.getString(4), blockRow.getInteger(5)));
                                break;
                            case BlockConstants.BLOCK_TYPE_TELEPORT:
                                block.getBlockInfo().getStructure().setMaterial(BlockConstants.STRUCTURE_MATERIAL_HOLLOW);
                                block.getBlockInfo().getStructure().setLayer(BlockConstants.STRUCTURE_LAYER_BOTTOM_DECORATION);
                                WorldCoordinate to = new WorldCoordinate(blockRow.getInteger(4),
                                        new IntegerCoordinate(blockRow.getInteger(5),
                                                blockRow.getInteger(6)),
                                        new Coordinate(BigDecimal.valueOf(blockRow.getInteger(7)),
                                                BigDecimal.valueOf(blockRow.getInteger(8))));
                                world.getTeleportMap().put(block.getBlockInfo().getId(), to);
                                break;
                            case BlockConstants.BLOCK_TYPE_CONTAINER:
                                BagInfo bagInfo = new BagInfo();
                                bagInfo.setId(block.getBlockInfo().getId());
                                world.getBagInfoMap().put(block.getBlockInfo().getId(), bagInfo);
                                break;
                            default:
                                break;
                        }
                    }
                }
                newRegion.getScenes().put(newScene.getSceneCoordinate(), newScene);
            }
            world.getRegionMap().put(regionNo, newRegion);
        }
    }

//    private void registerInteractiveBlocks(GameWorld world) {
//        world.getRegionMap().values().forEach(region ->
//                region.getScenes().values().forEach(scene ->
//                        scene.getBlocks().forEach(block -> {
//            String id = UUID.randomUUID().toString();
//            block.setId(id);
//            Block worldBlock = BlockUtil.convertBlock2Block(block, region.getRegionNo(),
//                    scene.getSceneCoordinate(), block);
//            if (BlockConstants.BLOCK_TYPE_CONTAINER == block.getType()) {
//                BagInfo bagInfo = new BagInfo();
//                bagInfo.setId(id);
//                world.getBagInfoMap().put(id, bagInfo);
//            }
//            if (BlockConstants.BLOCK_TYPE_DROP == block.getType()
//                    || BlockConstants.BLOCK_TYPE_TELEPORT == block.getType()
//                    || BlockUtil.checkBlockTypeInteractive(block.getType())) {
//                world.getBlockMap().put(id, worldBlock);
//            }
//        })));
//    }

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
    public ResponseEntity<String> addEvent(String userCode, Block eventBlock) {
        JSONObject rst = ContentUtil.generateRst();
        GameWorld world = userService.getWorldByUserCode(userCode);
        if (null == world) {
            return ResponseEntity.badRequest().body(JSON.toJSONString(ErrorUtil.ERROR_1016));
        }
        List<Block> playerInfoList = world.getCreatureMap().values().stream()
                .filter(creature -> playerService.validateActiveness(world, creature.getBlockInfo().getId()))
                .filter(creature -> checkEventCondition(eventBlock, creature))
                .collect(Collectors.toList());
        activateEvent(world, eventBlock, playerInfoList);
        return ResponseEntity.ok().body(rst.toString());
    }

    /**
     * This method is only for the beginning of event
     * @param eventBlock
     * @param blocker
     * @return
     */
    private boolean checkEventCondition(final Block eventBlock, final Block blocker) {
        return eventBlock.getWorldCoordinate().getRegionNo() == blocker.getWorldCoordinate().getRegionNo()
                && checkEventConditionByEventCode(eventBlock, blocker);
    }

    private boolean checkEventConditionByEventCode(final Block eventBlock, final Block blocker) {
        GameWorld world = userService.getWorldByUserCode(eventBlock.getBlockInfo().getId());
        Map<Integer, Region> regionMap = world.getRegionMap();
        Block from = world.getCreatureMap().get(eventBlock.getBlockInfo().getId());
        PlayerInfo fromPlayerInfo = from.getPlayerInfo();
        boolean rst = true;
        switch (Integer.valueOf(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
                double angle1 = Objects.requireNonNull(BlockUtil.calculateAngle(regionMap.get(
                        eventBlock.getWorldCoordinate().getRegionNo()), from.getWorldCoordinate(),
                        blocker.getWorldCoordinate())).doubleValue();
                double angle2 = from.getMovementInfo().getFaceDirection().doubleValue();
                double deltaAngle = BlockUtil.compareAnglesInDegrees(angle1, angle2);
                rst = !eventBlock.getBlockInfo().getId().equals(blocker.getBlockInfo().getId())
                        && BlockUtil.calculateDistance(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                from.getWorldCoordinate(), blocker.getWorldCoordinate())
                        .compareTo(SkillConstants.SKILL_RANGE_MELEE) <= 0
                        && deltaAngle < SkillConstants.SKILL_ANGLE_MELEE_MAX.doubleValue();
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                Random random = new Random();
                double gaussianValue = random.nextGaussian();

                // 将生成的值转换成指定的均值和标准差
                BigDecimal shakingAngle = BigDecimal.valueOf(gaussianValue * (fromPlayerInfo.getPrecisionMax() - fromPlayerInfo.getPrecision()) / fromPlayerInfo.getPrecisionMax());
                rst = !eventBlock.getBlockInfo().getId().equals(blocker.getBlockInfo().getId())
                        && BlockUtil.calculateDistance(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                from.getWorldCoordinate(), blocker.getWorldCoordinate())
                        .compareTo(SkillConstants.SKILL_RANGE_SHOOT) <= 0
                        && BlockUtil.detectLineSquareCollision(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                        from, from.getMovementInfo().getFaceDirection().add(shakingAngle), blocker)
                        && BlockUtil.compareAnglesInDegrees(
                        BlockUtil.calculateAngle(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                from.getWorldCoordinate(), blocker.getWorldCoordinate()).doubleValue(),
                        from.getMovementInfo().getFaceDirection().doubleValue()) < 135D;
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                rst = BlockUtil.calculateDistance(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                eventBlock.getWorldCoordinate(), blocker.getWorldCoordinate())
                        .compareTo(SkillConstants.SKILL_RANGE_EXPLODE) <= 0;
                break;
            default:
                break;
        }
        return rst;
    }

    private void activateEvent(GameWorld world, final Block eventBlock, final List<Block> playerList) {
        Random random = new Random();
        Map<Integer, Region> regionMap = world.getRegionMap();
        Map<String, Block> creatureMap = world.getCreatureMap();
        WorldEvent worldEvent;
        int changedHp = SkillUtil.calculateChangedHp(Integer.valueOf(eventBlock.getBlockInfo().getCode()));
        switch (Integer.valueOf(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_MINE:
            case GamePalConstants.EVENT_CODE_FIRE:
            case GamePalConstants.EVENT_CODE_WATER:
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_HEAL:
                playerService.changeHp(eventBlock.getBlockInfo().getId(), changedHp, false);
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_MELEE_KICK:
            case GamePalConstants.EVENT_CODE_MELEE_SCRATCH:
            case GamePalConstants.EVENT_CODE_MELEE_CLEAVE:
            case GamePalConstants.EVENT_CODE_MELEE_STAB:
                playerList.forEach(player -> {
                    PlayerInfo playerInfo = player.getPlayerInfo();
                    int damageValue = changedHp;
                    if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_BLOCKED] != 0) {
                        damageValue /= 2;
                    }
                    playerService.damageHp(player.getBlockInfo().getId(), eventBlock.getBlockInfo().getId(), damageValue, false);
                    WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                            player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            BigDecimal.valueOf(random.nextDouble() / 2));
                    WorldEvent worldEvent1 = BlockUtil.createWorldEvent(player.getBlockInfo().getId(),
                            GamePalConstants.EVENT_CODE_BLEED, bleedWc);
                    world.getEventQueue().add(worldEvent1);
                });
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                List<Block> activatedBlockList = calculateBallisticBlocks(eventBlock, playerList);
                activatedBlockList.forEach((Block activatedBlock) -> {
                    BlockUtil.copyWorldCoordinate(activatedBlock.getWorldCoordinate(), eventBlock.getWorldCoordinate());
                    if (activatedBlock.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                            && creatureMap.containsKey(activatedBlock.getBlockInfo().getId())
                            && playerService.validateActiveness(world, activatedBlock.getBlockInfo().getId())) {
                        playerService.damageHp(activatedBlock.getBlockInfo().getId(), eventBlock.getBlockInfo().getId(),
                                changedHp, false);
                        WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                                world.getRegionMap().get(activatedBlock.getWorldCoordinate().getRegionNo()),
                                activatedBlock.getWorldCoordinate(),
                                BigDecimal.valueOf(random.nextDouble() * 360),
                                BigDecimal.valueOf(random.nextDouble() / 2));
                        WorldEvent worldEvent1 = BlockUtil.createWorldEvent(activatedBlock.getBlockInfo().getId(),
                                GamePalConstants.EVENT_CODE_BLEED, bleedWc);
                        world.getEventQueue().add(worldEvent1);
                    }
                });
                // Shake the final position of event 24/04/05
                WorldCoordinate shakenWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()), eventBlock.getWorldCoordinate(),
                        BigDecimal.valueOf(random.nextDouble() * 360), BigDecimal.valueOf(random.nextDouble() / 2));
                BlockUtil.copyWorldCoordinate(shakenWc, eventBlock.getWorldCoordinate());
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                if (Integer.valueOf(eventBlock.getBlockInfo().getCode()) == GamePalConstants.EVENT_CODE_SHOOT_SLUG
                        || Integer.valueOf(eventBlock.getBlockInfo().getCode()) == GamePalConstants.EVENT_CODE_SHOOT_MAGNUM
                        || Integer.valueOf(eventBlock.getBlockInfo().getCode()) == GamePalConstants.EVENT_CODE_SHOOT_ROCKET) {
                    Block from = creatureMap.get(eventBlock.getBlockInfo().getId());
                    WorldCoordinate sparkWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(from.getWorldCoordinate().getRegionNo()),
                            from.getWorldCoordinate(), from.getMovementInfo().getFaceDirection().add(BigDecimal.valueOf(
                                    SkillConstants.SKILL_ANGLE_SHOOT_MAX.doubleValue() * 2
                                            * (random.nextDouble() - 0.5D))), BigDecimal.ONE);
                    worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                            GamePalConstants.EVENT_CODE_SPARK, sparkWc);
                    world.getEventQueue().add(worldEvent);
                } else if (Integer.valueOf(eventBlock.getBlockInfo().getCode()) == GamePalConstants.EVENT_CODE_SHOOT_ROCKET) {
                    addEvent(eventBlock.getBlockInfo().getId(), BlockUtil.convertEvent2WorldBlock(
                            world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()), eventBlock.getBlockInfo().getId(),
                            GamePalConstants.EVENT_CODE_EXPLODE, eventBlock.getWorldCoordinate()));
                    // Add tail smoke 24/03/16
                    BigDecimal tailSmokeLength = BlockUtil.calculateDistance(regionMap.get(
                            eventBlock.getWorldCoordinate().getRegionNo()),
                            creatureMap.get(eventBlock.getBlockInfo().getId()).getWorldCoordinate(),
                            eventBlock.getWorldCoordinate());
                    int tailSmokeAmount = tailSmokeLength.intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                            creatureMap.get(eventBlock.getBlockInfo().getId()).getWorldCoordinate(),
                            eventBlock.getWorldCoordinate(), tailSmokeAmount);
                    equidistantPoints.stream()
                            .forEach(tailSmokeCoordinate -> {
                                BlockUtil.fixWorldCoordinate(regionMap.get(
                                        eventBlock.getWorldCoordinate().getRegionNo()), tailSmokeCoordinate);
                                WorldEvent worldEvent1 = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                                        GamePalConstants.EVENT_CODE_TAIL_SMOKE, tailSmokeCoordinate);
                                world.getEventQueue().add(worldEvent1);
                            });
                }
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
                BigDecimal flameLength = BlockUtil.calculateDistance(regionMap.get(
                        eventBlock.getWorldCoordinate().getRegionNo()),
                        creatureMap.get(eventBlock.getBlockInfo().getId()).getWorldCoordinate(),
                        eventBlock.getWorldCoordinate());
                int flameAmount = flameLength.subtract(SkillConstants.SKILL_RANGE_SHOOT_FIRE_MIN).max(BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(2)).intValue() + 1;
                List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                        regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                        creatureMap.get(eventBlock.getBlockInfo().getId()).getWorldCoordinate(),
                        eventBlock.getWorldCoordinate(), flameAmount);
                equidistantPoints.stream()
                        .forEach(flameCoordinate -> {
                            BlockUtil.fixWorldCoordinate(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                    flameCoordinate);
                            WorldEvent worldEvent1 = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                                    GamePalConstants.EVENT_CODE_FIRE, flameCoordinate);
                            world.getEventQueue().add(worldEvent1);
                        });
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                playerList.stream().forEach(player -> {
                    playerService.damageHp(player.getBlockInfo().getId(), eventBlock.getBlockInfo().getId(),
                            changedHp, false);
                    WorldCoordinate bleedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                            world.getRegionMap().get(player.getWorldCoordinate().getRegionNo()),
                            player.getWorldCoordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            BigDecimal.valueOf(random.nextDouble() / 2));
                    WorldEvent worldEvent1 = BlockUtil.createWorldEvent(player.getBlockInfo().getId(),
                            GamePalConstants.EVENT_CODE_BLEED, bleedWc);
                    world.getEventQueue().add(worldEvent1);
                });
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_CURSE:
                playerList.stream()
                        .filter(player -> !player.getBlockInfo().getId().equals(eventBlock.getBlockInfo().getId()))
                        .filter(player -> BlockUtil.calculateDistance(
                                world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                        eventBlock.getWorldCoordinate(), player.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CURSE) < 0)
                        .forEach(worldBlock ->  {
                            Block player = creatureMap.get(worldBlock.getBlockInfo().getId());
                            PlayerInfo playerInfo = player.getPlayerInfo();
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] != -1) {
                                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_SAD] = GamePalConstants.BUFF_DEFAULT_FRAME_SAD;
                                playerService.changeVp(worldBlock.getBlockInfo().getId(), 0, true);
                            }
                        });
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                break;
            case GamePalConstants.EVENT_CODE_CHEER:
                playerList.stream()
                        .filter(player -> !player.getBlockInfo().getId().equals(eventBlock.getBlockInfo().getId()))
                        .filter(player -> BlockUtil.calculateDistance(
                                        world.getRegionMap().get(eventBlock.getWorldCoordinate().getRegionNo()),
                                        eventBlock.getWorldCoordinate(), player.getWorldCoordinate())
                                .compareTo(SkillConstants.SKILL_RANGE_CHEER) < 0)
                        .forEach(worldBlock ->  {
                            Block player = creatureMap.get(worldBlock.getBlockInfo().getId());
                            PlayerInfo playerInfo = player.getPlayerInfo();
                            if (playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HAPPY] != -1) {
                                playerInfo.getBuff()[GamePalConstants.BUFF_CODE_HAPPY] = GamePalConstants.BUFF_DEFAULT_FRAME_HAPPY;
                                playerService.changeVp(worldBlock.getBlockInfo().getId(), playerInfo.getVpMax(), true);
                            }
                        });
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        Integer.valueOf(eventBlock.getBlockInfo().getCode()), eventBlock.getWorldCoordinate());
                world.getEventQueue().add(worldEvent);
                break;
            default:
                break;
        }
        addEventNoise(world, eventBlock);
    }

    private void addEventNoise(GameWorld world, final Block eventBlock) {
        switch (Integer.valueOf(eventBlock.getBlockInfo().getCode())) {
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
            case GamePalConstants.EVENT_CODE_EXPLODE:
                world.getEventQueue().add(BlockUtil.createWorldEvent(eventBlock.getBlockInfo().getId(),
                        GamePalConstants.EVENT_CODE_NOISE,
                        world.getCreatureMap().get(eventBlock.getBlockInfo().getId()).getWorldCoordinate()));
                break;
            default:
                break;
        }
    }

    private List<Block> calculateBallisticBlocks(final Block eventBlock, final List<Block> playerInfoList) {
        GameWorld world = userService.getWorldByUserCode(eventBlock.getBlockInfo().getId());
        Block eventPlayer = world.getCreatureMap().get(eventBlock.getBlockInfo().getId());
        Map<Integer, Region> regionMap = world.getRegionMap();
        List<Block> activatedBlockList = new ArrayList<>();
        activatedBlockList.add(new Block(eventBlock));
        // Detect the nearest blocker (including playerInfos) 24/03/21
        List<IntegerCoordinate> preSelectedSceneCoordinates = BlockUtil.preSelectSceneCoordinates(
                regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()), eventPlayer.getWorldCoordinate(),
                eventBlock.getWorldCoordinate());
        List<Block> preSelectedBlocks = new ArrayList<>(playerInfoList);
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
            regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()).getScenes().get(sceneCoordinate).getBlocks().stream()
                    .filter(blocker ->
                            BlockConstants.STRUCTURE_MATERIAL_HOLLOW != blocker.getBlockInfo().getStructure().getMaterial())
                    .forEach(preSelectedBlocks::add));
        preSelectedBlocks.stream()
                .filter(wb -> checkEventCondition(eventBlock, wb))
                .forEach(wb -> {
                    BigDecimal distanceOld =
                            BlockUtil.calculateDistance(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                    eventPlayer.getWorldCoordinate(),
                                    activatedBlockList.get(activatedBlockList.size() - 1).getWorldCoordinate());
                    BigDecimal distanceNew =
                            BlockUtil.calculateDistance(regionMap.get(eventBlock.getWorldCoordinate().getRegionNo()),
                                    eventPlayer.getWorldCoordinate(), wb.getWorldCoordinate());
                    if (null != distanceOld && null != distanceNew && distanceOld.compareTo(distanceNew) > 0) {
                        if (wb.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_PLAYER
                                || eventBlock.getBlockInfo().getType() != SkillConstants.SKILL_CODE_SHOOT_MAGNUM) {
                            activatedBlockList.clear();
                        }
                        activatedBlockList.add(new Block(wb));
                    }
                });
        return activatedBlockList;
    }

    @Override
    public void updateEvents(GameWorld world) {
        Map<Integer, Region> regionMap = world.getRegionMap();
        // Clear events from scene 24/08/07
        regionMap.values().stream()
                .filter(region -> null != region.getScenes())
                .filter(region -> !region.getScenes().isEmpty())
                .forEach(region -> region.getScenes().values()
                        .forEach(scene -> scene.getEvents().clear())
        );
        Queue<WorldEvent> eventQueue = world.getEventQueue();
        WorldEvent tailEvent = new WorldEvent();
        eventQueue.add(tailEvent);
        while (tailEvent != eventQueue.peek()) {
            WorldEvent newEvent = updateEvent(world, eventQueue.poll());
            if (null != newEvent) {
                eventQueue.add(newEvent);
                expandByCoordinate(world, null, newEvent, 0);
                regionMap.get(newEvent.getRegionNo()).getScenes().get(newEvent.getSceneCoordinate()).getEvents()
                        .add(BlockUtil.convertWorldEvent2Event(newEvent));
//                if (!regionMap.containsKey(newEvent.getRegionNo())) {
//                    logger.error(ErrorUtil.ERROR_1027);
//                } else {
//                    if (!regionMap.get(newEvent.getRegionNo()).getScenes().containsKey(newEvent.getSceneCoordinate())) {
//                        // Detect and expand scenes after updating event's location
//                        expandScene(world, newEvent, 1);
//                    }
//                    regionMap.get(newEvent.getRegionNo()).getScenes().get(newEvent.getSceneCoordinate()).getEvents()
//                            .add(BlockUtil.convertWorldEvent2Event(newEvent));
//                }
            }
        }
        eventQueue.poll();
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

    private WorldEvent updateEvent(GameWorld world, WorldEvent oldEvent) {
        WorldEvent newEvent = new WorldEvent();
        newEvent.setUserCode(oldEvent.getUserCode());
        newEvent.setCode(oldEvent.getCode());
        updateEventLocation(world, oldEvent, newEvent);
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
        switch (oldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_MINE:
                if (world.getCreatureMap().values().stream()
                        .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                        .filter(player -> !StringUtils.equals(newEvent.getUserCode(), player.getBlockInfo().getId()))
                        .anyMatch(player -> {
                            BigDecimal distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(newEvent.getRegionNo()), newEvent, player.getWorldCoordinate());
                            return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_MINE) < 0;
                        })) {
                    addEvent(oldEvent.getUserCode(), BlockUtil.convertEvent2WorldBlock(
                            world.getRegionMap().get(oldEvent.getRegionNo()), oldEvent.getUserCode(),
                            GamePalConstants.EVENT_CODE_EXPLODE, oldEvent));
                    return null;
                }
                break;
            case GamePalConstants.EVENT_CODE_FIRE:
                // Extinguished by water
                if (world.getEventQueue().stream()
                        .filter(worldEvent -> worldEvent.getCode() == GamePalConstants.EVENT_CODE_WATER)
                        .anyMatch(worldEvent -> {
                            BigDecimal distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(newEvent.getRegionNo()), newEvent, worldEvent);
                            return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                        })) {
                    return null;
                }
                // Burn players
                world.getCreatureMap().values().stream()
                        .filter(player -> playerService.validateActiveness(world, player.getBlockInfo().getId()))
                        .filter(player -> {
                            BigDecimal distance = BlockUtil.calculateDistance(
                                    world.getRegionMap().get(newEvent.getRegionNo()), newEvent, player.getWorldCoordinate());
                            return null != distance && distance.compareTo(SkillConstants.SKILL_RANGE_FIRE) < 0;
                        })
                        .forEach(player -> {
                            playerService.damageHp(player.getBlockInfo().getId(), newEvent.getUserCode(),
                                    SkillUtil.calculateChangedHp(newEvent.getCode()), false);
                            WorldEvent worldEvent1 = BlockUtil.createWorldEvent(player.getBlockInfo().getId(),
                                    GamePalConstants.EVENT_CODE_BLEED, player.getWorldCoordinate());
                            world.getEventQueue().add(worldEvent1);
                        });
                break;
            default:
                break;
        }
        return newEvent;
    }

    private void updateEventLocation(GameWorld world, WorldEvent oldEvent, WorldEvent newEvent) {
        switch (oldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_BLOCK:
            case GamePalConstants.EVENT_CODE_HEAL:
            case GamePalConstants.EVENT_CODE_SACRIFICE:
            case GamePalConstants.EVENT_CODE_DISTURB:
                // Stick with playerInfo
                BlockUtil.copyWorldCoordinate(world.getCreatureMap().get(oldEvent.getUserCode()).getWorldCoordinate(),
                        newEvent);
                break;
            default:
                // Keep its position
                BlockUtil.copyWorldCoordinate(oldEvent, newEvent);
                break;
        }
    }

    @Override
    public void updateWorldTime(GameWorld world, int increment) {
        world.setWorldTime((world.getWorldTime() + increment) % GamePalConstants.MAX_WORLD_TIME);
    }

    @Override
    public ResponseEntity<String> putBlock(GameWorld world, Block worldBlock) {
        expandByCoordinate(world, null, worldBlock.getWorldCoordinate(), 0);
        Region region = world.getRegionMap().get(worldBlock.getWorldCoordinate().getRegionNo());
        Scene scene = region.getScenes().get(worldBlock.getWorldCoordinate().getSceneCoordinate());
        // TODO
        return null;
    }
}
