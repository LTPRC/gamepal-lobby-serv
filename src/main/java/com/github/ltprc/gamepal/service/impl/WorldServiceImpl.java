package com.github.ltprc.gamepal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.game.Game;
import com.github.ltprc.gamepal.model.item.*;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.*;
import com.github.ltprc.gamepal.model.npc.NpcBrain;
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
import java.util.stream.Collectors;

@Transactional
@Service
public class WorldServiceImpl implements WorldService {

    private static final Log logger = LogFactory.getLog(WorldServiceImpl.class);
    private Map<String, GameWorld> worldMap = new LinkedHashMap<>(); // worldCode, world (We only allow 1 world now 24/02/16)
    private Map<String, Item> itemMap = new HashMap<>(); // itemNo, item

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SceneManager sceneManager;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private MovementManager movementManager;

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

    private void initiateWorld(GameWorld world) {
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
        loadBlocks(world);
        initiateGame(world);
    }

    private void loadScenes(GameWorld world) {
        JSONArray regions = ContentUtil.jsonFile2JSONArray("src/main/resources/json/regions.json");
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
                                case 2:
                                    block.setType(GamePalConstants.BLOCK_TYPE_WALL);
                                    break;
                                case 3:
                                    block.setType(GamePalConstants.BLOCK_TYPE_CEILING);
                                    break;
                                case 1:
                                default:
                                    block.setType(GamePalConstants.BLOCK_TYPE_GROUND);
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
            world.getRegionMap().put(regionNo, newRegion);
        }
    }

    private void loadBlocks(GameWorld world) {
        world.getRegionMap().entrySet().stream().forEach(entry1 -> {
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
        List<WorldBlock> playerInfoList = world.getPlayerInfoMap().entrySet().stream()
                .map(entry -> entry.getValue())
                .filter(playerInfo -> checkEventCondition(eventBlock, playerInfo))
                .collect(Collectors.toList());
        activateEvent(eventBlock, playerInfoList);
        return ResponseEntity.ok().body(rst.toString());
    }

    private boolean checkEventCondition(final WorldBlock eventBlock, final WorldBlock blocker) {
        GameWorld world = userService.getWorldByUserCode(eventBlock.getId());
        Map<Integer, Region> regionMap = world.getRegionMap();
        PlayerInfo fromPlayerInfo = world.getPlayerInfoMap().get(eventBlock.getId());
        boolean rst = eventBlock.getRegionNo() == blocker.getRegionNo()
                && (blocker.getType() != GamePalConstants.BLOCK_TYPE_PLAYER
                || (GamePalConstants.PLAYER_STATUS_RUNNING == fromPlayerInfo.getPlayerStatus()
                && 0 == fromPlayerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD]));
        switch (Integer.valueOf(eventBlock.getCode())) {
            case GamePalConstants.EVENT_CODE_FIRE:
                rst &= BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), eventBlock, blocker)
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
                rst &= !eventBlock.getId().equals(blocker.getId())
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
                rst &= !eventBlock.getId().equals(blocker.getId())
                        && BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), fromPlayerInfo, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_SHOOT) <= 0
                        && BlockUtil.detectLineSquareCollision(regionMap.get(eventBlock.getRegionNo()),
                        fromPlayerInfo, fromPlayerInfo.getFaceDirection().add(shakingAngle), blocker,
                        blocker.getType())
                        && BlockUtil.compareAnglesInDegrees(
                        BlockUtil.calculateAngle(regionMap.get(eventBlock.getRegionNo()), fromPlayerInfo,
                                blocker).doubleValue(), fromPlayerInfo.getFaceDirection().doubleValue()) < 135D;
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                rst &= BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()), eventBlock, blocker)
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
                            && StringUtils.isNotBlank(activatedWorldBlock.getId())) {
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
                WorldCoordinate shakedWc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                        regionMap.get(eventBlock.getRegionNo()), eventBlock,
                        BigDecimal.valueOf(random.nextDouble() * 360), BigDecimal.valueOf(random.nextDouble() / 2));
                BlockUtil.copyWorldCoordinate(shakedWc, eventBlock);
                worldEvent = BlockUtil.createWorldEvent(eventBlock.getId(), Integer.valueOf(eventBlock.getCode()),
                        eventBlock);
                world.getEventQueue().add(worldEvent);
                if (Integer.valueOf(eventBlock.getCode()) == GamePalConstants.EVENT_CODE_SHOOT_ROCKET) {
                    addEvent(eventBlock.getId(), BlockUtil.createEventWorldBlock(
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
        preSelectedSceneCoordinates.stream().forEach(sceneCoordinate -> {
            regionMap.get(eventBlock.getRegionNo()).getScenes().get(sceneCoordinate).getBlocks().stream()
                    .filter(blocker -> BlockUtil.checkBlockSolid(blocker.getType()))
                    .forEach(blocker -> {
                        WorldCoordinate wc = BlockUtil.convertCoordinate2WorldCoordinate(
                                regionMap.get(eventBlock.getRegionNo()), sceneCoordinate, blocker);
                        WorldBlock wb = new WorldBlock(blocker.getType(), blocker.getId(), blocker.getCode(), wc);
                        preSelectedWorldBlocks.add(wb);
                    });
        });
        preSelectedWorldBlocks.stream()
                .filter(wb -> checkEventCondition(eventBlock, wb))
                .forEach(wb -> {
                    BigDecimal distanceOld =
                            BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()),
                                    eventPlayerInfo, activatedWorldBlockList.get(activatedWorldBlockList.size() - 1));
                    BigDecimal distanceNew =
                            BlockUtil.calculateDistance(regionMap.get(eventBlock.getRegionNo()),
                                    eventPlayerInfo, wb);
                    if (distanceOld.compareTo(distanceNew) > 0) {
                        if (wb.getType() != GamePalConstants.BLOCK_TYPE_PLAYER
                                || eventBlock.getType() != GamePalConstants.SKILL_CODE_SHOOT_MAGNUM) {
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
                scene.getValue().setEvents(new ArrayList<>())
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
        expandScene(world, worldCoordinate, 1);
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
            sceneManager.fillScene(region, worldCoordinate.getSceneCoordinate(), GamePalConstants.REGION_INDEX_GRASSLAND);
        }
        if (depth > 0) {
            WorldCoordinate newWorldCoordinate = new WorldCoordinate();
            BlockUtil.copyWorldCoordinate(worldCoordinate, newWorldCoordinate);
            for (int i = - 1; i <= 1; i++) {
                for (int j = - 1; j <= 1; j++) {
                    if (worldCoordinate.getSceneCoordinate().getX() + i >= -GamePalConstants.SCENE_SCAN_MAX_RADIUS
                            && worldCoordinate.getSceneCoordinate().getX() + i <= GamePalConstants.SCENE_SCAN_MAX_RADIUS
                            && worldCoordinate.getSceneCoordinate().getY() + j >= -GamePalConstants.SCENE_SCAN_MAX_RADIUS
                            && worldCoordinate.getSceneCoordinate().getY() + j <= GamePalConstants.SCENE_SCAN_MAX_RADIUS) {
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

    @Deprecated
    private void triggerEvent(WorldEvent worldEvent) {
        GameWorld world = userService.getWorldByUserCode(worldEvent.getUserCode());
        if (null == world) {
            logger.error(ErrorUtil.ERROR_1016);
            return;
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        switch (worldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_FIRE:
                // Continuous event
                break;
            default:
                // Immediate event
                if (worldEvent.getFrame() != 0) {
                    return;
                }
        }
        switch (worldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_HEAL:
                // Self event
                playerService.changeHp(worldEvent.getUserCode(), GamePalConstants.EVENT_HEAL_HEAL, false);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_HIT_FIRE:
            case GamePalConstants.EVENT_CODE_HIT_ICE:
            case GamePalConstants.EVENT_CODE_HIT_ELECTRICITY:
                // Non-self event
                world.getPlayerInfoMap().entrySet().stream()
                        .filter(entry -> !entry.getValue().getId().equals(worldEvent.getUserCode())
                                && entry.getValue().getRegionNo() == worldEvent.getRegionNo()
                                && GamePalConstants.PLAYER_STATUS_RUNNING == entry.getValue().getPlayerStatus()
                                && 0 == entry.getValue().getBuff()[GamePalConstants.BUFF_CODE_DEAD])
                        .filter(entry -> checkEvent(worldEvent, entry.getValue()))
                        .forEach(entry -> activateEvent(worldEvent, entry.getValue().getId()));
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
                // Non-self event, move to the nearest player/blocker
                PlayerInfo eventPlayerInfo = world.getPlayerInfoMap().get(worldEvent.getUserCode());
                WorldCoordinate nearestPlayerCoordinate = new WorldCoordinate();
                BlockUtil.copyWorldCoordinate(worldEvent, nearestPlayerCoordinate);
                WorldBlock activatedWorldBlock = new WorldBlock();
                // Detect the nearest blocker (including playerInfos) 24/03/21
                List<IntegerCoordinate> preSelectedSceneCoordinates = BlockUtil.preSelectSceneCoordinates(
                        regionMap.get(worldEvent.getRegionNo()), eventPlayerInfo, nearestPlayerCoordinate);
                List<WorldBlock> preSelectedWorldBlocks = new ArrayList<>();
                preSelectedSceneCoordinates.stream().forEach(sceneCoordinate -> {
                    world.getOnlineMap().entrySet().stream()
                            .filter(entry -> {
                                PlayerInfo playerInfo = world.getPlayerInfoMap().get(entry.getKey());
                                return !entry.getKey().equals(worldEvent.getUserCode())
                                        && playerInfo.getRegionNo() == worldEvent.getRegionNo()
                                        && GamePalConstants.PLAYER_STATUS_RUNNING == playerInfo.getPlayerStatus()
                                        && 0 == playerInfo.getBuff()[GamePalConstants.BUFF_CODE_DEAD];})
                            .forEach(entry ->
                                    preSelectedWorldBlocks.add(world.getPlayerInfoMap().get(entry.getKey())));
                    regionMap.get(worldEvent.getRegionNo()).getScenes().get(sceneCoordinate).getBlocks().stream()
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_GROUND)
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_DROP)
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_TELEPORT)
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_GROUND_DECORATION)
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_WALL_DECORATION)
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_CEILING_DECORATION)
                            .filter(blocker -> blocker.getType() != GamePalConstants.BLOCK_TYPE_HOLLOW_WALL)
                            .forEach(blocker -> {
                                WorldCoordinate wc = BlockUtil.convertCoordinate2WorldCoordinate(
                                        regionMap.get(worldEvent.getRegionNo()), sceneCoordinate, blocker);
                                WorldBlock wb = new WorldBlock();
                                BlockUtil.copyWorldCoordinate(wc, wb);
                                wb.setType(blocker.getType());
                                wb.setId(blocker.getId());
                                wb.setCode(blocker.getCode());
                                preSelectedWorldBlocks.add(wb);
                            });
                });
                preSelectedWorldBlocks.stream()
                        .filter(wb -> checkEvent(worldEvent, wb))
                        .forEach(wb -> {
                            BigDecimal distanceOld =
                                    BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()),
                                                eventPlayerInfo, nearestPlayerCoordinate);
                            BigDecimal distanceNew =
                                    BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()),
                                                eventPlayerInfo, wb);
                            if (distanceOld.compareTo(distanceNew) > 0) {
                                BlockUtil.copyWorldCoordinate(wb, nearestPlayerCoordinate);
                                activatedWorldBlock.setId(GamePalConstants.BLOCK_TYPE_PLAYER == wb.getType()
                                        ? wb.getId() : null);
                            }
                        });
                // Shake the final position of event 24/03/22
                nearestPlayerCoordinate.getCoordinate().setX(nearestPlayerCoordinate.getCoordinate().getX()
                        .add(BigDecimal.valueOf(Math.random() - 0.5D)));
                nearestPlayerCoordinate.getCoordinate().setY(nearestPlayerCoordinate.getCoordinate().getY()
                        .add(BigDecimal.valueOf(Math.random() - 0.5D)));
                BlockUtil.fixWorldCoordinate(regionMap.get(worldEvent.getRegionNo()), nearestPlayerCoordinate);
                BlockUtil.copyWorldCoordinate(nearestPlayerCoordinate, worldEvent);
                if (StringUtils.isNotBlank(activatedWorldBlock.getId())) {
                    activateEvent(worldEvent, activatedWorldBlock.getId());
                }
                if (worldEvent.getCode() == GamePalConstants.EVENT_CODE_SHOOT_ROCKET) {
                    // Add tail smoke 24/03/16
                    BigDecimal tailSmokeLength = BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()),
                            eventPlayerInfo, worldEvent);
                    int tailSmokeAmount = tailSmokeLength.intValue() + 1;
                    List<WorldCoordinate> equidistantPoints = BlockUtil.collectEquidistantPoints(
                            regionMap.get(worldEvent.getRegionNo()), eventPlayerInfo, worldEvent, tailSmokeAmount);
                    equidistantPoints.stream()
                            .forEach(tailSmokeCoordinate -> {
                                WorldBlock tailSmokeEventBlock = BlockUtil.createEventWorldBlock(
                                        world.getRegionMap().get(tailSmokeCoordinate.getRegionNo()),
                                        eventPlayerInfo.getId(),
                                        GamePalConstants.EVENT_CODE_TAIL_SMOKE, tailSmokeCoordinate);
                                BlockUtil.fixWorldCoordinate(regionMap.get(worldEvent.getRegionNo()), tailSmokeEventBlock);
                                addEvent(worldEvent.getUserCode(), tailSmokeEventBlock);
                            });
                }
                break;
            default:
                // Global event
                world.getPlayerInfoMap().entrySet().stream()
                        .filter(entry -> entry.getValue().getRegionNo() == worldEvent.getRegionNo())
                        .filter(entry -> checkEvent(worldEvent, entry.getValue()))
                        .forEach(entry -> activateEvent(worldEvent, entry.getValue().getId()));
                break;
        }
    }

    /**
     * checkEvent
     * @param worldEvent event block
     * @param blocker player/blocker block
     * @return whether this player can be hit by the event
     */
    @Deprecated
    private boolean checkEvent(final WorldEvent worldEvent, final WorldBlock blocker) {
        GameWorld world = userService.getWorldByUserCode(worldEvent.getUserCode());
        Map<Integer, Region> regionMap = world.getRegionMap();
        PlayerInfo fromPlayerInfo = world.getPlayerInfoMap().get(worldEvent.getUserCode());
        boolean rst = false;
        switch (worldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_FIRE:
                if (BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()), worldEvent, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_FIRE) <= 0) {
                    rst = true;
                }
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_HIT_FIRE:
            case GamePalConstants.EVENT_CODE_HIT_ICE:
            case GamePalConstants.EVENT_CODE_HIT_ELECTRICITY:
                double angle1 = BlockUtil.calculateAngle(regionMap.get(worldEvent.getRegionNo()), fromPlayerInfo,
                        blocker).doubleValue();
                double angle2 = fromPlayerInfo.getFaceDirection().doubleValue();
                double deltaAngle = BlockUtil.compareAnglesInDegrees(angle1, angle2);
                if (BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()), fromPlayerInfo, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_MELEE) <= 0
                        && deltaAngle < GamePalConstants.EVENT_MAX_ANGLE_MELEE.doubleValue()) {
                    rst = true;
                }
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
                BigDecimal shakingAngle = BigDecimal.valueOf(Math.random() * 2 - 1);
//                if (blocker.getType() == GamePalConstants.BLOCK_TYPE_PLAYER
//                        || blocker.getType() == GamePalConstants.BLOCK_TYPE_TREE) {
//                    // Detect figure: round
//                    if (BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()), fromPlayerInfo, blocker)
//                            .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_SHOOT) <= 0
//                            && BlockUtil.calculateBallisticDistance(regionMap.get(worldEvent.getRegionNo()),
//                            fromPlayerInfo, fromPlayerInfo.getFaceDirection().add(shakingAngle), blocker).doubleValue()
//                            < GamePalConstants.PLAYER_RADIUS.doubleValue()
//                            && BlockUtil.compareAnglesInDegrees(
//                            BlockUtil.calculateAngle(regionMap.get(worldEvent.getRegionNo()), fromPlayerInfo,
//                                    blocker).doubleValue(), fromPlayerInfo.getFaceDirection().doubleValue()) < 90D) {
//                        rst = true;
//                    }
//                } else {
                    // Detect figure: square
                    if (BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()), fromPlayerInfo, blocker)
                            .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_SHOOT) <= 0
                            && BlockUtil.detectLineSquareCollision(regionMap.get(worldEvent.getRegionNo()),
                            fromPlayerInfo, fromPlayerInfo.getFaceDirection().add(shakingAngle), blocker,
                            blocker.getType())
                            && BlockUtil.compareAnglesInDegrees(
                            BlockUtil.calculateAngle(regionMap.get(worldEvent.getRegionNo()), fromPlayerInfo,
                                    blocker).doubleValue(), fromPlayerInfo.getFaceDirection().doubleValue()) < 135D) {
                        rst = true;
                    }
//                }
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                if (BlockUtil.calculateDistance(regionMap.get(worldEvent.getRegionNo()), worldEvent, blocker)
                        .compareTo(GamePalConstants.EVENT_MAX_DISTANCE_EXPLODE) <= 0) {
                    rst = true;
                }
                break;
            default:
                rst = true;
                break;
        }
        return rst;
    }

    /**
     * activateEvent
     * @param worldEvent event block
     * @param userCode
     */
    @Deprecated
    private void activateEvent(WorldEvent worldEvent, String userCode) {
        Random random = new Random();
        GameWorld world = userService.getWorldByUserCode(userCode);
        PlayerInfo playerInfo = world.getPlayerInfoMap().get(userCode);
        WorldCoordinate wc = BlockUtil.locateCoordinateWithDirectionAndDistance(
                world.getRegionMap().get(playerInfo.getRegionNo()), playerInfo,
                BigDecimal.valueOf(random.nextDouble() * 360), BigDecimal.valueOf(random.nextDouble() / 2));
        WorldBlock bleedEventBlock;
        switch (worldEvent.getCode()) {
            case GamePalConstants.EVENT_CODE_FIRE:
                playerService.damageHp(userCode, worldEvent.getUserCode(),
                        -GamePalConstants.EVENT_DAMAGE_PER_FRAME_FIRE, false);
                break;
            case GamePalConstants.EVENT_CODE_MELEE_HIT:
            case GamePalConstants.EVENT_CODE_HIT_FIRE:
            case GamePalConstants.EVENT_CODE_HIT_ICE:
            case GamePalConstants.EVENT_CODE_HIT_ELECTRICITY:
                int damageValue = -GamePalConstants.EVENT_DAMAGE_MELEE;
                if (userService.getWorldByUserCode(userCode).getEventQueue().stream()
                        .anyMatch(event -> GamePalConstants.EVENT_CODE_BLOCK == event.getCode()
                                && userCode.equals(event.getUserCode()))) {
                    // This player is blocking 24/03/15
                    damageValue /= 2;
                }
                playerService.damageHp(userCode, worldEvent.getUserCode(), damageValue, false);
                bleedEventBlock = BlockUtil.createEventWorldBlock(world.getRegionMap().get(wc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_BLEED, wc);
                addEvent(userCode, bleedEventBlock);
                break;
            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
                bleedEventBlock = BlockUtil.createEventWorldBlock(world.getRegionMap().get(wc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_BLEED, wc);
                addEvent(userCode, bleedEventBlock);
                playerService.damageHp(userCode, worldEvent.getUserCode(),
                        -GamePalConstants.EVENT_DAMAGE_SHOOT, false);
                break;
            case GamePalConstants.EVENT_CODE_EXPLODE:
                bleedEventBlock = BlockUtil.createEventWorldBlock(world.getRegionMap().get(wc.getRegionNo()), userCode,
                        GamePalConstants.EVENT_CODE_BLEED, wc);
                addEvent(userCode, bleedEventBlock);
                playerService.damageHp(userCode, worldEvent.getUserCode(),
                        -GamePalConstants.EVENT_DAMAGE_EXPLODE, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void updateNpcMovement() {
        worldMap.entrySet().stream().forEach(entry1 -> {
            // Run NPC tasks
            Map<String, NpcBrain> npcBrainMap = entry1.getValue().getNpcBrainMap();
            npcBrainMap.entrySet().stream()
                    .filter(entry2 -> entry1.getValue().getPlayerInfoMap().get(entry2.getKey()).getPlayerStatus()
                            == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .filter(entry2 -> entry1.getValue().getPlayerInfoMap().get(entry2.getKey())
                            .getBuff()[GamePalConstants.BUFF_CODE_DEAD] == 0)
                    .forEach(entry2 -> {
                        String npcUserCode = entry2.getKey();
                        JSONObject observeReq = new JSONObject();
                        observeReq.put("npcTaskType", GamePalConstants.NPC_TASK_TYPE_OBSERVE);
                        observeReq.put("userCode", npcUserCode);
                        JSONObject observeResp = npcManager.runNpcTask(observeReq);
                        WorldCoordinate wc = observeResp.getObject("wc", WorldCoordinate.class);
                        if (null != wc) {
                            JSONObject moveReq = new JSONObject();
                            moveReq.put("npcTaskType", GamePalConstants.NPC_TASK_TYPE_MOVE);
                            moveReq.put("userCode", npcUserCode);
                            moveReq.put("wc", wc);
                            JSONObject moveResp = npcManager.runNpcTask(moveReq);
                        } else {
                            JSONObject idleReq = new JSONObject();
                            idleReq.put("npcTaskType", GamePalConstants.NPC_TASK_TYPE_IDLE);
                            idleReq.put("userCode", npcUserCode);
                            JSONObject idleResp = npcManager.runNpcTask(idleReq);
                        }
                    });
            // Settle NPC speed
            Map<String, PlayerInfo> playerInfoMap = entry1.getValue().getPlayerInfoMap();
            playerInfoMap.entrySet().stream()
                    .filter(entry2 -> entry2.getValue().getPlayerType() == GamePalConstants.PLAYER_TYPE_AI)
                    .filter(entry2 -> entry2.getValue().getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                    .forEach(entry2 -> movementManager.settleSpeed(entry2.getKey(), entry2.getValue()));
        });
    }
}
