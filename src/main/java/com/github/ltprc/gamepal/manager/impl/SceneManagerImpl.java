package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.FlagConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.config.RegionConstants;
import com.github.ltprc.gamepal.config.SceneConstants;
import com.github.ltprc.gamepal.config.SkillConstants;
import com.github.ltprc.gamepal.factory.BlockFactory;
import com.github.ltprc.gamepal.factory.SceneFactory;
import com.github.ltprc.gamepal.manager.EventManager;
import com.github.ltprc.gamepal.manager.FarmManager;
import com.github.ltprc.gamepal.manager.InteractionManager;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.FarmInfo;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.block.StructuredBlock;
import com.github.ltprc.gamepal.model.map.coordinate.Coordinate;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.scene.GravitatedStack;
import com.github.ltprc.gamepal.model.map.scene.Scene;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.coordinate.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WebSocketService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
public class SceneManagerImpl implements SceneManager {

    private static final Log logger = LogFactory.getLog(SceneManagerImpl.class);
    private static final Random random = new Random();

    @Autowired
    private UserService userService;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MovementManager movementManager;

    @Autowired
    private FarmManager farmManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private WorldService worldService;

    @Autowired
    private InteractionManager interactionManager;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void fillScene(final GameWorld world, final Region region, final IntegerCoordinate sceneCoordinate) {
        if (region.getScenes().containsKey(sceneCoordinate)
                || !RegionUtil.validateSceneCoordinate(region, sceneCoordinate)) {
            return;
        }
        Scene scene = SceneFactory.createScene(region, sceneCoordinate, "Auto Scene (" + sceneCoordinate.getX()
                + "," + sceneCoordinate.getY() + ")");
        region.getScenes().put(sceneCoordinate, scene);
        int terrainCode = BlockConstants.BLOCK_CODE_BLACK;
        switch (region.getType()) {
            case RegionConstants.REGION_TYPE_ALL_DIRT:
            case RegionConstants.REGION_TYPE_ALL_SAND:
            case RegionConstants.REGION_TYPE_ALL_GRASS:
            case RegionConstants.REGION_TYPE_ALL_SNOW:
            case RegionConstants.REGION_TYPE_ALL_SWAMP:
            case RegionConstants.REGION_TYPE_ALL_ROUGH:
            case RegionConstants.REGION_TYPE_ALL_SUBTERRANEAN:
            case RegionConstants.REGION_TYPE_ALL_LAVA:
            case RegionConstants.REGION_TYPE_ALL_WATER_SHALLOW:
            case RegionConstants.REGION_TYPE_ALL_WATER_MEDIUM:
            case RegionConstants.REGION_TYPE_ALL_WATER_DEEP:
                terrainCode = region.getType();
                break;
            case RegionConstants.REGION_TYPE_EMPTY:
            case RegionConstants.REGION_TYPE_ISLAND:
                terrainCode = region.getTerrainMap().getOrDefault(sceneCoordinate, BlockConstants.BLOCK_CODE_BLACK);
                break;
            default:
                break;
        }
        fillSceneTemplate(world, region, scene, terrainCode);
    }

    private Scene fillSceneTemplate(GameWorld world, final Region region, final Scene scene, final int blockCode) {
        for (int i = 0; i <= region.getWidth(); i++) {
            for (int j = 0; j <= region.getHeight(); j++) {
                scene.getGrid()[i][j] = 1001;
            }
        }
        IntegerCoordinate sceneCoordinate = scene.getSceneCoordinate();
        Scene scene1;
        for (int l = 0; l <= region.getWidth(); l++) {
            for (int k = 0; k <= region.getHeight(); k++) {
                scene.getGrid()[l][k] = blockCode;
            }
        }
        // Area 0,0
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() - 1));
        if (null != scene1) {
            scene.getGrid()[0][0] = scene1.getGrid()[region.getWidth()][region.getHeight()];
        } else {
            scene.getGrid()[0][0] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() - 1), blockCode);
        }
        // Area 2,0
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() - 1));
        if (null != scene1) {
            scene.getGrid()[region.getWidth()][0] = scene1.getGrid()[0][region.getHeight()];
        } else {
            scene.getGrid()[region.getWidth()][0] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() - 1), blockCode);
        }
        // Area 0,2
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() + 1));
        if (null != scene1) {
            scene.getGrid()[0][region.getHeight()] = scene1.getGrid()[region.getWidth()][0];
        } else {
            scene.getGrid()[0][region.getHeight()] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() + 1), blockCode);
        }
        // Area 2,2
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() + 1));
        if (null != scene1) {
            scene.getGrid()[region.getWidth()][region.getHeight()] = scene1.getGrid()[0][0];
        } else {
            scene.getGrid()[region.getWidth()][region.getHeight()] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() + 1), blockCode);
        }
        for (int i = 1; i < region.getWidth(); i++) {
            // Area 1,0
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() - 1));
            if (null != scene1) {
                scene.getGrid()[i][0] = scene1.getGrid()[i][region.getHeight()];
            } else {
                scene.getGrid()[i][0] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1), blockCode);
            }
            // Area 1,2
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1));
            if (null != scene1) {
                scene.getGrid()[i][region.getHeight()] = scene1.getGrid()[i][0];
            } else {
                scene.getGrid()[i][region.getHeight()] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1), blockCode);
            }
        }
        for (int i = 1; i < region.getHeight(); i++) {
            // Area 0,1
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY()));
            if (null != scene1) {
                scene.getGrid()[i][region.getHeight()] = scene1.getGrid()[region.getHeight()][i];
            } else {
                scene.getGrid()[i][region.getHeight()] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY()), blockCode);
            }
            // Area 2,1
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY()));
            if (null != scene1) {
                scene.getGrid()[region.getHeight()][i] = scene1.getGrid()[0][i];
            } else {
                scene.getGrid()[region.getHeight()][i] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY()), blockCode);
            }
        }
        if (blockCode == BlockConstants.BLOCK_CODE_BLACK) {
            scene.getBlocks().values().forEach(block -> removeBlock(world, block, false));
        } else {
            SceneUtil.initiateSceneGravitatedStacks(region, scene);
            // Pollute from 4 sides
            polluteBlockCode(region, scene, blockCode);
            addSceneObjects(world, region, scene);
            addSceneAnimals(world, region, scene);
        }
        return scene;
    }

    /**
     * Pollution is not related with altitude
     * @param region
     * @param scene
     * @param defaultBlockCode
     */
    private void polluteBlockCode(final Region region, final Scene scene, final int defaultBlockCode) {
        for (int l = 1; l < region.getWidth(); l++) {
            for (int k = 1; k < region.getHeight(); k++) {
                int upCode = scene.getGrid()[l][0];
                int leftCode = scene.getGrid()[0][k];
                int rightCode = scene.getGrid()[region.getWidth()][k];
                int downCode = scene.getGrid()[l][region.getHeight()];
                int upWeight = region.getHeight() - k;
                int leftWeight = region.getWidth() - l;
                int rightWeight = l;
                int downWeight = k;
                upWeight = Math.max(0, upWeight - region.getHeight() / 2);
                leftWeight = Math.max(0, leftWeight - region.getWidth() / 2);
                rightWeight = Math.max(0, rightWeight - region.getWidth() / 2);
                downWeight = Math.max(0, downWeight - region.getHeight() / 2);
                int val = random.nextInt(upWeight + leftWeight + rightWeight + downWeight + 1);
                if (val < upWeight) {
                    scene.getGrid()[l][k] = upCode;
                    continue;
                } else {
                    val -= upWeight;
                }
                if (val < leftWeight) {
                    scene.getGrid()[l][k] = leftCode;
                    continue;
                } else {
                    val -= rightWeight;
                }
                if (val < rightWeight) {
                    scene.getGrid()[l][k] = rightCode;
                    continue;
                } else {
                    val -= leftWeight;
                }
                if (val < downWeight) {
                    scene.getGrid()[l][k] = downCode;
                    continue;
                } else {
                    val -= downWeight;
                }
                scene.getGrid()[l][k] = defaultBlockCode;
            }
        }
    }

    private void addSceneObjects(GameWorld world, RegionInfo regionInfo, Scene scene) {
        for (int i = 0; i < regionInfo.getWidth() - 1; i++) {
            for (int j = 0; j < regionInfo.getHeight() - 1; j++) {
                // TODO Last row/column may cause overlap issue with player 25/01/19
                switch (random.nextInt(4)) {
                    case 0:
                        int upleftBlockCode = scene.getGrid()[i][j];
                        addSceneObject(world, regionInfo, scene, upleftBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    case 1:
                        int uprightBlockCode = scene.getGrid()[i + 1][j];
                        addSceneObject(world, regionInfo, scene, uprightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    case 2:
                        int downleftBlockCode = scene.getGrid()[i][j + 1];
                        addSceneObject(world, regionInfo, scene, downleftBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    case 3:
                        int downrightBlockCode = scene.getGrid()[i + 1][j + 1];
                        addSceneObject(world, regionInfo, scene, downrightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void addSceneObject(GameWorld world, RegionInfo regionInfo, Scene scene, int blockCode, BigDecimal x,
                                BigDecimal y) {
        Coordinate coordinate = new Coordinate(
                x.subtract(BigDecimal.valueOf(0.5D)).add(BigDecimal.valueOf(random.nextDouble())),
                y.subtract(BigDecimal.valueOf(0.5D)).add(BigDecimal.valueOf(random.nextDouble())),
                regionInfo.getAltitude());
        WorldCoordinate worldCoordinate = new WorldCoordinate(regionInfo.getRegionNo(), scene.getSceneCoordinate(),
                coordinate);
        Map<Integer, Integer> weightMap;
        switch (blockCode) {
            case BlockConstants.BLOCK_CODE_DIRT:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_DIRT;
                break;
            case BlockConstants.BLOCK_CODE_SAND:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_SAND;
                break;
            case BlockConstants.BLOCK_CODE_GRASS:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_GRASS;
                break;
            case BlockConstants.BLOCK_CODE_SWAMP:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_SWAMP;
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_ROUGH;
                break;
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_SUBTERRANEAN;
                break;
            case BlockConstants.BLOCK_CODE_LAVA:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_LAVA;
                break;
            case BlockConstants.BLOCK_CODE_SNOW:
            case BlockConstants.BLOCK_CODE_WATER_SHALLOW:
            case BlockConstants.BLOCK_CODE_WATER_MEDIUM:
            case BlockConstants.BLOCK_CODE_WATER_DEEP:
            case BlockConstants.BLOCK_CODE_BLACK:
            default:
                weightMap = SceneConstants.OBJECT_WEIGHT_MAP_DEFAULT;
                break;
        }
        int randomInt = random.nextInt(weightMap.values().stream().mapToInt(Integer::intValue).sum());
        List<Map.Entry<Integer, Integer>> weightList = new ArrayList<>(weightMap.entrySet());
        for (int i = 0; i < weightList.size() && randomInt >= 0; i++) {
            if (randomInt < weightList.get(i).getValue()
                    && weightList.get(i).getKey() != BlockConstants.BLOCK_CODE_BLACK) {
                addOtherBlock(world, worldCoordinate, weightList.get(i).getKey());
                break;
            }
            randomInt -= weightList.get(i).getValue();
        }
    }

    private void addSceneAnimals(GameWorld world, RegionInfo regionInfo, Scene scene) {
        int blockCode = BlockConstants.BLOCK_CODE_BLACK;
        for (int i = 0; i < regionInfo.getWidth(); i++) {
            for (int j = 0; j < regionInfo.getHeight(); j++) {
                switch (random.nextInt(4)) {
                    case 0:
                        blockCode = scene.getGrid()[i][j];
                        break;
                    case 1:
                        blockCode = scene.getGrid()[i + 1][j];
                        break;
                    case 2:
                        blockCode = scene.getGrid()[i][j + 1];
                        break;
                    case 3:
                        blockCode = scene.getGrid()[i + 1][j + 1];
                        break;
                    default:
                        break;
                }
                addSceneAnimal(world, regionInfo, scene, blockCode, BigDecimal.valueOf(i), BigDecimal.valueOf(j));
            }
        }
    }

    private void addSceneAnimal(GameWorld world, RegionInfo regionInfo, Scene scene, int blockCode, BigDecimal x,
                                BigDecimal y) {
        Map<Integer, Integer> weightMap;
        switch (blockCode) {
            case BlockConstants.BLOCK_CODE_DIRT:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_DIRT;
                break;
            case BlockConstants.BLOCK_CODE_GRASS:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_GRASS;
                break;
            case BlockConstants.BLOCK_CODE_SNOW:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_SNOW;
                break;
            case BlockConstants.BLOCK_CODE_SWAMP:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_SWAMP;
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_ROUGH;
                break;
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_SUBTERRANEAN;
                break;
            case BlockConstants.BLOCK_CODE_SAND:
            case BlockConstants.BLOCK_CODE_LAVA:
            case BlockConstants.BLOCK_CODE_WATER_SHALLOW:
            case BlockConstants.BLOCK_CODE_WATER_MEDIUM:
            case BlockConstants.BLOCK_CODE_WATER_DEEP:
            case BlockConstants.BLOCK_CODE_BLACK:
            default:
                weightMap = SceneConstants.ANIMAL_WEIGHT_MAP_DEFAULT;
                break;
        }
        int randomInt = random.nextInt(weightMap.values().stream().mapToInt(Integer::intValue).sum());
        List<Map.Entry<Integer, Integer>> weightList = new ArrayList<>(weightMap.entrySet());
        for (int i = 0; i < weightList.size() && randomInt >= 0; i++) {
            if (randomInt < weightList.get(i).getValue()
                    && BlockConstants.BLOCK_CODE_BLACK != weightList.get(i).getKey()) {
                String animalUserCode = UUID.randomUUID().toString();
                Block animal = npcManager.createCreature(world, GamePalConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                PlayerInfo playerInfo = world.getPlayerInfoMap().get(animal.getBlockInfo().getId());
                playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                int skinColor = weightList.get(i).getKey();
                playerInfo.setSkinColor(skinColor);
                WorldCoordinate worldCoordinate = new WorldCoordinate(regionInfo.getRegionNo(),
                        scene.getSceneCoordinate(),
                        new Coordinate(
                                x.add(BigDecimal.valueOf(random.nextDouble() * 0.49D)),
                                y.add(BigDecimal.valueOf(random.nextDouble() * 0.49D)),
                                regionInfo.getAltitude()));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                break;
            }
            randomInt -= weightList.get(i).getValue();
        }
    }

    @Override
    public Queue<StructuredBlock> collectSurroundingBlocks(final GameWorld world, final Block player,
                                                           final int sceneScanRadius) {
        Queue<StructuredBlock> rankingQueue = collectSurroundingBlocksFromScenes(world, player, sceneScanRadius);
        rankingQueue.addAll(collectSurroundingBlocksFromCreatureMap(world, player, sceneScanRadius));
        return rankingQueue;
    }

    private Queue<StructuredBlock> collectSurroundingBlocksFromScenes(final GameWorld world, final Block player,
                                                                      final int sceneScanRadius) {
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        if (null == region) {
            logger.error(ErrorUtil.ERROR_1027);
            return new LinkedList<>();
        }
        Queue<StructuredBlock> rankingQueue = BlockFactory.createRankingQueue(region);
        IntegerCoordinate sceneCoordinate = player.getWorldCoordinate().getSceneCoordinate();
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        // Collect blocks from SCENE_SCAN_RADIUS * SCENE_SCAN_RADIUS scenes 24/03/16
        for (int i = sceneCoordinate.getY() - sceneScanRadius;
             i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
            for (int j = sceneCoordinate.getX() - sceneScanRadius;
                 j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
                final IntegerCoordinate newSceneCoordinate = new IntegerCoordinate(j, i);
                if (!region.getScenes().containsKey(newSceneCoordinate)
                        || !RegionUtil.validateSceneCoordinate(region, sceneCoordinate)) {
                    continue;
                }
                Scene scene = region.getScenes().get(newSceneCoordinate);
                scene.getBlocks().values().stream()
                        .filter(block -> PlayerInfoUtil.checkPerceptionCondition(region, player,
                                player.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                        ? world.getPlayerInfoMap().get(player.getBlockInfo().getId()).getPerceptionInfo()
                                        : null, block))
                        .forEach(block -> {
                            rankingQueue.add(new StructuredBlock(block,
                                    structureMap.getOrDefault(block.getBlockInfo().getCode(), new Structure())));
                            collectTransformedBlocks(world, player.getBlockInfo().getId(), rankingQueue, block);
                        });
            }
        }
        return rankingQueue;
    }

    private Queue<StructuredBlock> collectSurroundingBlocksFromCreatureMap(final GameWorld world, final Block player,
                                                                           final int sceneScanRadius) {
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        if (null == region) {
            logger.error(ErrorUtil.ERROR_1027);
            return new LinkedList<>();
        }
        Queue<StructuredBlock> rankingQueue = BlockFactory.createRankingQueue(region);
        // Collect detected creature blocks
        Map<String, Block> creatureMap = world.getCreatureMap();
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        creatureMap.values().stream()
                // Creature blocks contain running player or NPC 24/03/25
                .filter(player1 -> playerService.validateActiveness(world, player1.getBlockInfo().getId()))
                .filter(player1 -> SkillUtil.isSceneDetected(player, player1.getWorldCoordinate(), sceneScanRadius))
                .filter(block -> PlayerInfoUtil.checkPerceptionCondition(region, player,
                        player.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                ? world.getPlayerInfoMap().get(player.getBlockInfo().getId()).getPerceptionInfo()
                                : null, block))
                .forEach(player1 -> rankingQueue.add(new StructuredBlock(player1,
                        structureMap.getOrDefault(player1.getBlockInfo().getCode(), new Structure()))));
        return rankingQueue;
    }

    private void collectTransformedBlocks(final GameWorld world, String collectorId,
                                          Queue<StructuredBlock> rankingQueue, Block block) {
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_DROP:
                rankingQueue.add(new StructuredBlock(new Block(block.getWorldCoordinate(),
                        BlockFactory.createBlockInfoByCode(BlockConstants.BLOCK_CODE_DROP_SHADOW), new MovementInfo()),
                        structureMap.getOrDefault(block.getBlockInfo().getCode(), new Structure())));
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                Optional<Block> cropBlock = farmManager.generateCropByFarm(world, block);
                cropBlock.ifPresent(cropBlock1 -> rankingQueue.add(new StructuredBlock(cropBlock1,
                        structureMap.getOrDefault(cropBlock1.getBlockInfo().getCode(), new Structure()))));
                break;
            case BlockConstants.BLOCK_TYPE_TRAP:
                switch (block.getBlockInfo().getCode()) {
                    case BlockConstants.BLOCK_CODE_MINE:
                        if (StringUtils.equals(collectorId, world.getSourceMap().get(block.getBlockInfo().getId()))) {
                            Block mineBlock = new Block(block.getWorldCoordinate(),
                                    BlockFactory.createBlockInfoByCode(BlockConstants.BLOCK_CODE_MINE_FLAG),
                                    new MovementInfo());
                            rankingQueue.add(new StructuredBlock(mineBlock,
                                    structureMap.getOrDefault(BlockConstants.BLOCK_CODE_MINE_FLAG, new Structure())));
                        }
                    break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR:
            case BlockConstants.BLOCK_TYPE_WALL:
                Integer crackCode = null;
                if (block.getBlockInfo().getHp().get() < block.getBlockInfo().getHpMax().get() * 0.25) {
                    crackCode = BlockConstants.BLOCK_CODE_CRACK_3;
                } else if (block.getBlockInfo().getHp().get() < block.getBlockInfo().getHpMax().get() * 0.5) {
                    crackCode = BlockConstants.BLOCK_CODE_CRACK_2;
                } else if (block.getBlockInfo().getHp().get() < block.getBlockInfo().getHpMax().get() * 0.75) {
                    crackCode = BlockConstants.BLOCK_CODE_CRACK_1;
                }
                if (null != crackCode) {
                    Block crackBlock = new Block(block.getWorldCoordinate(),
                            BlockFactory.createBlockInfoByCode(crackCode), new MovementInfo());
                    crackBlock.getWorldCoordinate().getCoordinate().setZ(
                            crackBlock.getWorldCoordinate().getCoordinate().getZ()
                                    .add(structureMap.getOrDefault(block.getBlockInfo().getCode(), new Structure())
                                            .getShape().getRadius().getZ())
                                    .subtract(BigDecimal.ONE));
                    rankingQueue.add(new StructuredBlock(crackBlock,
                            structureMap.getOrDefault(crackCode, new Structure())));
                }
                break;
            default:
                break;
        }
    }

    public List<Block> collideBlocks(final GameWorld world, WorldCoordinate fromWorldCoordinate, Block eventBlock,
                                     boolean relocate) {
        List<Block> preSelectedBlocks = collectBlocks(world, fromWorldCoordinate, eventBlock);
        BigDecimal planarDistance;
        BigDecimal verticalDistance;
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        Structure structure1 = structureMap.getOrDefault(eventBlock.getBlockInfo().getCode(), new Structure());
        switch (eventBlock.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_SHOOT:
                return preSelectedBlocks.stream()
                        .filter(blocker -> movementManager.detectLinearCollision(world, fromWorldCoordinate, eventBlock,
                                blocker, relocate))
                        .collect(Collectors.toList());
            case BlockConstants.BLOCK_TYPE_MELEE:
                BigDecimal sectorAngle = SkillConstants.SKILL_ANGLE_MELEE_MAX;
                return preSelectedBlocks.stream()
                        .filter(blocker -> movementManager.detectSectorInfluence(world, fromWorldCoordinate, eventBlock,
                                blocker, sectorAngle))
                        .collect(Collectors.toList());
            case BlockConstants.BLOCK_TYPE_PLASMA:
            case BlockConstants.BLOCK_TYPE_EXPLOSION:
                switch (eventBlock.getBlockInfo().getCode()) {
                    case BlockConstants.BLOCK_CODE_EXPLODE:
                        planarDistance = BlockConstants.EXPLODE_RADIUS;
                        verticalDistance = BlockConstants.EXPLODE_RADIUS;
                        break;
                    case BlockConstants.BLOCK_CODE_FIRE:
                        planarDistance = BlockConstants.FIRE_PLANAR_DISTANCE;
                        verticalDistance = BlockConstants.FIRE_VERTICAL_DISTANCE;
                        break;
                    case BlockConstants.BLOCK_CODE_SPRAY:
                        planarDistance = BlockConstants.SPRAY_PLANAR_DISTANCE;
                        verticalDistance = BlockConstants.SPRAY_VERTICAL_DISTANCE;
                        break;
                    default:
                        planarDistance = BigDecimal.ZERO;
                        verticalDistance = BigDecimal.ZERO;
                        break;
                }
                return preSelectedBlocks.stream()
                        .filter(blocker -> movementManager.detectCylinderInfluence(world, fromWorldCoordinate, eventBlock,
                                blocker, planarDistance, verticalDistance))
                        .collect(Collectors.toList());
            default:
                return preSelectedBlocks;
        }
    }

    @Override
    public List<Block> collectBlocks(final GameWorld world, WorldCoordinate fromWorldCoordinate, Block eventBlock) {
        WorldCoordinate worldCoordinate = eventBlock.getWorldCoordinate();
        if (worldCoordinate.getRegionNo() != fromWorldCoordinate.getRegionNo()) {
            return new ArrayList<>();
        }
        Map<Integer, Region> regionMap = world.getRegionMap();
        Region region = regionMap.get(worldCoordinate.getRegionNo());
        Set<IntegerCoordinate> preSelectedSceneCoordinates =
                BlockUtil.preSelectLinearSceneCoordinates(region, fromWorldCoordinate, eventBlock.getWorldCoordinate());
        String sourceId = world.getSourceMap().containsKey(eventBlock.getBlockInfo().getId())
                ? world.getSourceMap().get(eventBlock.getBlockInfo().getId())
                : eventBlock.getBlockInfo().getId();
        // Pre-select blocks including creatures
        List<Block> preSelectedBlocks = world.getCreatureMap().values().stream()
                .filter(creature -> !StringUtils.equals(creature.getBlockInfo().getId(), sourceId))
                .filter(creature -> creature.getWorldCoordinate().getRegionNo() == region.getRegionNo())
                .filter(creature -> preSelectedSceneCoordinates.contains(creature.getWorldCoordinate().getSceneCoordinate()))
                .filter(creature -> playerService.validateActiveness(world, creature.getBlockInfo().getId()))
                .collect(Collectors.toList());
        // Collect all collided blocks
        preSelectedSceneCoordinates.forEach(sceneCoordinate ->
                preSelectedBlocks.addAll(region.getScenes().get(sceneCoordinate).getBlocks().values()));
        return preSelectedBlocks;
    }

    @Override
    public int[][] collectGridsByUserCode(String userCode, int sceneScanRadius) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        IntegerCoordinate sceneCoordinate = player.getWorldCoordinate().getSceneCoordinate();
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        int[][] grids = new int[region.getWidth() * (sceneScanRadius * 2 + 1) + 1]
                [region.getHeight() * (sceneScanRadius * 2 + 1) + 1];
        for (int i = sceneCoordinate.getY() - sceneScanRadius; i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
            for (int j = sceneCoordinate.getX() - sceneScanRadius; j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
                final IntegerCoordinate newSceneCoordinate = new IntegerCoordinate(j, i);
                if (!region.getScenes().containsKey(newSceneCoordinate)
                        || !RegionUtil.validateSceneCoordinate(region, sceneCoordinate)) {
                    continue;
                }
                Scene scene = region.getScenes().get(newSceneCoordinate);
                for (int l = 0; l <= region.getWidth(); l++) {
                    for (int k = 0; k <= region.getHeight(); k++) {
                        int val = BlockConstants.BLOCK_CODE_BLACK;
                        if (null != scene && null != scene.getGrid()) {
                            val = scene.getGrid()[l][k];
                        }
                        grids[l + (j - sceneCoordinate.getX() + sceneScanRadius) * region.getWidth()]
                                [k + (i - sceneCoordinate.getY() + sceneScanRadius) * region.getHeight()] = val;
                    }
                }
            }
        }
        return grids;
    }

    @Override
    public BigDecimal[][] collectAltitudesByUserCode(String userCode, int sceneScanRadius) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, Block> creatureMap = world.getCreatureMap();
        Block player = creatureMap.get(userCode);
        IntegerCoordinate sceneCoordinate = player.getWorldCoordinate().getSceneCoordinate();
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        BigDecimal[][] altitudes = new BigDecimal[region.getWidth() * (sceneScanRadius * 2 + 1)]
                [region.getHeight() * (sceneScanRadius * 2 + 1)];
        for (int i = sceneCoordinate.getY() - sceneScanRadius; i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
            for (int j = sceneCoordinate.getX() - sceneScanRadius; j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
                final IntegerCoordinate newSceneCoordinate = new IntegerCoordinate(j, i);
                if (!region.getScenes().containsKey(newSceneCoordinate)
                        || !RegionUtil.validateSceneCoordinate(region, sceneCoordinate)) {
                    continue;
                }
                Scene scene = region.getScenes().get(newSceneCoordinate);
                for (int l = 0; l < region.getWidth(); l++) {
                    for (int k = 0; k < region.getHeight(); k++) {
                        BigDecimal val = region.getAltitude();
                        if (null != scene && null != scene.getGravitatedStacks()
                                && null != scene.getGravitatedStacks()[l]
                                && null != scene.getGravitatedStacks()[l][k]) {
                            val = scene.getGravitatedStacks()[l][k].getMinAltitude();
                        }
                        altitudes[l + (j - sceneCoordinate.getX() + sceneScanRadius) * region.getWidth()]
                                [k + (i - sceneCoordinate.getY() + sceneScanRadius) * region.getHeight()] = val;
                    }
                }
            }
        }
        return altitudes;
    }

    @Override
    public JSONObject convertBlock2OldBlockInstance(final GameWorld world, final String userCode, final Block block,
                                                    final boolean useWorldCoordinate, final long timestamp) {
        JSONObject rst = new JSONObject();
        if (useWorldCoordinate) {
            rst.putAll(JSON.parseObject(JSON.toJSONString(block.getWorldCoordinate())));
        } else {
            Region region = world.getRegionMap().get(block.getWorldCoordinate().getRegionNo());
            Block from = world.getCreatureMap().get(userCode);
            Coordinate coordinate = BlockUtil.adjustCoordinate(block.getWorldCoordinate().getCoordinate(),
                    BlockUtil.getCoordinateRelation(from.getWorldCoordinate().getSceneCoordinate(),
                            block.getWorldCoordinate().getSceneCoordinate()), region.getHeight(), region.getWidth());
            rst.putAll(JSON.parseObject(JSON.toJSONString(coordinate)));
        }
        rst.putAll(JSON.parseObject(JSON.toJSONString(block.getMovementInfo())));
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                rst.putAll(JSON.parseObject(JSON.toJSONString(world.getPlayerInfoMap().get(block.getBlockInfo().getId()))));
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                if (!world.getDropMap().containsKey(block.getBlockInfo().getId())) {
                    return null;
                }
                Map.Entry<String, Integer> entry = world.getDropMap().get(block.getBlockInfo().getId());
                rst.put("itemNo", entry.getKey());
                rst.put("amount", entry.getValue());
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                if (!world.getTeleportMap().containsKey(block.getBlockInfo().getId())) {
                    return null;
                }
                WorldCoordinate to = world.getTeleportMap().get(block.getBlockInfo().getId());
                rst.put("to", to);
                break;
//            case BlockConstants.BLOCK_TYPE_TRAP:
//                switch (block.getBlockInfo().getCode()) {
//                    case BlockConstants.BLOCK_CODE_MINE:
//                        if (StringUtils.equals(userCode, world.getSourceMap().get(block.getBlockInfo().getId()))) {
//                            // Only for the master of the mine
//                            block.getBlockInfo().setCode(BlockConstants.BLOCK_CODE_MINE_FLAG);
//                            webSocketService.resetPlayerBlockMapByUserAndBlock(world, userCode,
//                                    block.getBlockInfo().getId());
//                        }
//                        break;
//                    default:
//                        break;
//                }
//                break;
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
                rst.putAll(JSON.parseObject(JSON.toJSONString(world.getPlayerInfoMap().get(block.getBlockInfo().getId()))));
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                if (!world.getFarmMap().containsKey(block.getBlockInfo().getId())) {
                    return null;
                }
                FarmInfo farmInfo = world.getFarmMap().get(block.getBlockInfo().getId());
                rst.put("farmInfo", farmInfo);
                break;
            default:
                break;
        }
        rst.putAll(JSON.parseObject(JSON.toJSONString(block.getBlockInfo())));
        return rst;
    }

    /**
     * Added a random direction and default distance
     * @param world
     * @param worldCoordinate
     * @param drop
     * @return
     */
    @Override
    public Block addDropBlock(GameWorld world, WorldCoordinate worldCoordinate, Map.Entry<String, Integer> drop) {
        if (null == drop) {
            return null;
        }
        Block block = addOtherBlock(world, worldCoordinate, BlockConstants.BLOCK_CODE_DROP_DEFAULT);
        world.getDropMap().put(block.getBlockInfo().getId(), drop);
        Coordinate dropSpeed = new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(random.nextDouble() * BlockConstants.DROP_THROW_VERTICAL_SPEED_MAX.doubleValue()));
        movementManager.settleAcceleration(world, block, BlockUtil.locateCoordinateWithDirectionAndDistance(dropSpeed,
                BigDecimal.valueOf(random.nextDouble() * 360), BlockConstants.DROP_THROW_PLANAR_SPEED_MAX), null, null);
        return block;
    }

    @Override
    public Block addTeleportBlock(GameWorld world, WorldCoordinate worldCoordinate, final int blockCode, WorldCoordinate to) {
        Block block = addOtherBlock(world, worldCoordinate, blockCode);
        if (null != to) {
            world.getTeleportMap().put(block.getBlockInfo().getId(), to);
        }
        return block;
    }

    @Override
    public Block addTextDisplayBlock(GameWorld world, WorldCoordinate worldCoordinate, int blockCode, String textDisplay) {
        Block block = addOtherBlock(world, worldCoordinate, blockCode);
        if (StringUtils.isNotBlank(textDisplay)) {
            world.getTextDisplayMap().put(block.getBlockInfo().getId(), textDisplay);
        }
        return block;
    }

    /**
     * Add all types of blocks except player blocks
     * @param world
     * @param worldCoordinate
     * @param blockCode
     * @return
     */
    @Override
    public Block addOtherBlock(final GameWorld world, final WorldCoordinate worldCoordinate, final int blockCode) {
        BlockInfo blockInfo = BlockFactory.createBlockInfoByCode(blockCode);
        MovementInfo movementInfo = new MovementInfo();
        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
        updateBlockAltitude(world, block);
        switch (blockInfo.getType()) {
            case BlockConstants.BLOCK_TYPE_CONTAINER:
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
            case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                BagInfo bagInfo = new BagInfo();
                bagInfo.setId(block.getBlockInfo().getId());
                world.getBagInfoMap().put(block.getBlockInfo().getId(), bagInfo);
                userService.addUserIntoWorldMap(block.getBlockInfo().getId(), world.getId());
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                world.getFarmMap().put(block.getBlockInfo().getId(), new FarmInfo());
                break;
            case BlockConstants.BLOCK_TYPE_FLOOR:
            case BlockConstants.BLOCK_TYPE_WALL:
                addBlockIntoGravitatedStack(world, block);
                break;
            case BlockConstants.BLOCK_TYPE_WALL_DECORATION:
                Map<Integer, Structure> structureMap = worldService.getStructureMap();
                block.getWorldCoordinate().getCoordinate().setZ(block.getWorldCoordinate().getCoordinate().getZ()
                        .subtract(structureMap.getOrDefault(block.getBlockInfo().getCode(), new Structure())
                                .getShape().getRadius().getZ()));
                break;
            default:
                break;
        }
        registerBlock(world, block);
        return block;
    }

    private Block registerBlock(GameWorld world, Block block) {
        Region region = world.getRegionMap().get(block.getWorldCoordinate().getRegionNo());
        Scene scene = region.getScenes().get(block.getWorldCoordinate().getSceneCoordinate());
        scene.getBlocks().put(block.getBlockInfo().getId(), block);
        world.getBlockMap().put(block.getBlockInfo().getId(), block);
        return block;
    }

    @Override
    public boolean checkBlockSpace2Build(GameWorld world, Block block) {
        WorldCoordinate worldCoordinate = block.getWorldCoordinate();
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
        if (!SkillUtil.blockCode2Build(getGridBlockCode(world, worldCoordinate))) {
            return false;
        }
        return scene.getBlocks().values().stream()
                .filter(blocker -> !StringUtils.equals(block.getBlockInfo().getId(), blocker.getBlockInfo().getId()))
                .noneMatch(blocker -> movementManager.detectCollision(world, block, blocker, false));
    }

    @Override
    public void removeBlock(GameWorld world, Block block, boolean isDestroyed) {
        if (isDestroyed) {
            destroyBlock(world, block);
        }
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_FLOOR:
            case BlockConstants.BLOCK_TYPE_WALL:
                removeBlockFromGravitatedStack(world, block);
                break;
            default:
                break;
        }
        unregisterBlock(world, block);
    }

    private void unregisterBlock(GameWorld world, Block block) {
        WorldCoordinate worldCoordinate = block.getWorldCoordinate();
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_EFFECT:
                switch (block.getBlockInfo().getCode()) {
                    case BlockConstants.BLOCK_CODE_TIMED_BOMB:
                        eventManager.addEvent(world, BlockConstants.BLOCK_CODE_EXPLODE, world.getSourceMap().get(block.getBlockInfo().getId()), worldCoordinate);
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_PLAYER:
                playerService.destroyPlayer(block.getBlockInfo().getId());
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                world.getDropMap().remove(block.getBlockInfo().getId());
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                world.getTeleportMap().remove(block.getBlockInfo().getId());
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
            case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                world.getBagInfoMap().remove(block.getBlockInfo().getId());
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                world.getFarmMap().remove(block.getBlockInfo().getId());
                break;
            case BlockConstants.BLOCK_TYPE_TEXT_DISPLAY:
                world.getTextDisplayMap().remove(block.getBlockInfo().getId());
                break;
            default:
                break;
        }
        scene.getBlocks().remove(block.getBlockInfo().getId());
        world.getBlockMap().remove(block.getBlockInfo().getId());
        world.getSourceMap().remove(block.getBlockInfo().getId());
        if (BlockUtil.checkBlockTypeInteractive(block.getBlockInfo().getType())) {
            world.getInteractionInfoMap().entrySet().stream()
                    .filter(entry -> StringUtils.equals(entry.getValue().getId(), block.getBlockInfo().getId()))
                    .forEach(entry -> interactionManager.focusOnBlock(world, entry.getKey(), null));
        }
        webSocketService.resetPlayerBlockMapByBlock(world, block.getBlockInfo().getId());
    }

    private void destroyBlock(GameWorld world, Block block) {
        Block drop = null;
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_BED:
            case BlockConstants.BLOCK_TYPE_DRESSER:
            case BlockConstants.BLOCK_TYPE_STORAGE:
                for (int i = 0; i < 1 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m031", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
            case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
            case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                world.getBagInfoMap().get(block.getBlockInfo().getId()).getItems().entrySet()
                        .forEach(entry -> {
                            Block dropFromContainer = addDropBlock(world, block.getWorldCoordinate(), entry);
                        });
                switch (block.getBlockInfo().getType()) {
                    case BlockConstants.BLOCK_TYPE_CONTAINER:
                        for (int i = 0; i < 1 + random.nextInt(3); i++) {
                            drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m031", 1));
                        }
                        break;
                    case BlockConstants.BLOCK_TYPE_HUMAN_REMAIN_CONTAINER:
                        world.getPlayerInfoMap().remove(block.getBlockInfo().getId());
                        break;
                    case BlockConstants.BLOCK_TYPE_ANIMAL_REMAIN_CONTAINER:
                    default:
                        break;
                }
                break;
            case BlockConstants.BLOCK_TYPE_TOILET:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m007", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_COOKER:
            case BlockConstants.BLOCK_TYPE_BUILDING:
                for (int i = 0; i < 1 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m002", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_FARM:
                for (int i = 0; i < random.nextInt(2); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m018", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_SINK:
            case BlockConstants.BLOCK_TYPE_WORKSHOP:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                for (int i = 0; i < 1 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m001", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_SPEAKER:
                for (int i = 0; i < 1 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m003", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_TREE:
                for (int i = 0; i < 1 + random.nextInt(10); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m031", 1));
                }
                break;
            case BlockConstants.BLOCK_TYPE_ROCK:
                for (int i = 0; i < 1 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m002", 1));
                }
                int randomValue = random.nextInt(100);
                if (randomValue < 2) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m013", 1 + random.nextInt(2)));
                } else if (randomValue < 5) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m025", 1 + random.nextInt(2)));
                } else if (randomValue < 8) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m027", 1 + random.nextInt(2)));
                } else if (randomValue < 12) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m011", 1 + random.nextInt(2)));
                } else if (randomValue < 16) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m028", 1 + random.nextInt(2)));
                } else if (randomValue < 20) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m015", 1 + random.nextInt(2)));
                } else if (randomValue < 25) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m021", 1 + random.nextInt(2)));
                } else if (randomValue < 27) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m006", 1 + random.nextInt(2)));
                } else if (randomValue < 35) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m023", 1 + random.nextInt(2)));
                } else if (randomValue < 45) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m001", 1 + random.nextInt(2)));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public int getGridBlockCode(GameWorld world, WorldCoordinate worldCoordinate) {
        int code = BlockConstants.BLOCK_CODE_BLACK;
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
//        if (null != scene.getGrid() && null != scene.getGrid()[0]) {
            IntegerCoordinate gridCoordinate = BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate);
            code = scene.getGrid()[gridCoordinate.getX()][gridCoordinate.getY()];
//        }
        return code;
    }

    @Override
    public void setGridBlockCode(GameWorld world, WorldCoordinate worldCoordinate, int code) {
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
//        if (null != scene.getGrid() && null != scene.getGrid()[0]) {
            IntegerCoordinate gridCoordinate = BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate);
            scene.getGrid()[gridCoordinate.getX()][gridCoordinate.getY()] = code;
            IntegerCoordinate nearbySceneCoordinate = new IntegerCoordinate(worldCoordinate.getSceneCoordinate());
            if (gridCoordinate.getX() == 0) {
                nearbySceneCoordinate.setX(nearbySceneCoordinate.getX() - 1);
                gridCoordinate.setX(scene.getGrid()[0].length - 1);
            }
            if (gridCoordinate.getX() == scene.getGrid()[0].length - 1) {
                nearbySceneCoordinate.setX(nearbySceneCoordinate.getX() + 1);
                gridCoordinate.setX(0);
            }
            if (gridCoordinate.getY() == 0) {
                nearbySceneCoordinate.setY(nearbySceneCoordinate.getY() - 1);
                gridCoordinate.setY(scene.getGrid().length - 1);
            }
            if (gridCoordinate.getY() == scene.getGrid().length - 1) {
                nearbySceneCoordinate.setY(nearbySceneCoordinate.getY() + 1);
                gridCoordinate.setY(0);
            }
            scene = region.getScenes().get(nearbySceneCoordinate);
            if (null != scene && null != scene.getGrid() && null != scene.getGrid()[0]) {
                scene.getGrid()[scene.getGrid().length - 1][gridCoordinate.getY()] = code;
            }
//        }
        world.getCreatureMap().values().stream()
                .filter(creature -> creature.getWorldCoordinate().getRegionNo() == worldCoordinate.getRegionNo())
                .forEach(creature -> world.getFlagMap().get(creature.getBlockInfo().getId())
                        [FlagConstants.FLAG_UPDATE_GRIDS] = true);
    }

    @Override
    public BigDecimal getAltitude(GameWorld world, WorldCoordinate worldCoordinate) {
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        GravitatedStack gravitatedStack = locateGravitatedStack(world, worldCoordinate);
        return null == gravitatedStack ? region.getAltitude() : gravitatedStack.getMaxAltitude();
    }

    @Override
    public void updateBlockAltitude(GameWorld world, Block block) {
        if (BlockUtil.checkBlockTypeGravity(block.getBlockInfo().getType())) {
            block.getWorldCoordinate().getCoordinate().setZ(getAltitude(world, block.getWorldCoordinate()));
            webSocketService.resetPlayerBlockMapByBlock(world, block.getBlockInfo().getId());
        }
    }

    private GravitatedStack locateGravitatedStack(GameWorld world, WorldCoordinate worldCoordinate) {
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        IntegerCoordinate altitudeCoordinate = BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate);
//        WorldCoordinate locatedWorldCoordinate = new WorldCoordinate(worldCoordinate.getRegionNo(),
//                worldCoordinate.getSceneCoordinate(),
//                new Coordinate(BigDecimal.valueOf(altitudeCoordinate.getX()),
//                        BigDecimal.valueOf(altitudeCoordinate.getY()),
//                        worldCoordinate.getCoordinate().getZ()));
//        BlockUtil.fixWorldCoordinate(region, locatedWorldCoordinate);
        if (!region.getScenes().containsKey(worldCoordinate.getSceneCoordinate())) {
            fillScene(world, region, worldCoordinate.getSceneCoordinate());
        }
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
        return scene.getGravitatedStacks()[altitudeCoordinate.getX()][altitudeCoordinate.getY()];
    }

    private void addBlockIntoGravitatedStack(final GameWorld world, Block block) {
        WorldCoordinate worldCoordinate = block.getWorldCoordinate();
        GravitatedStack gravitatedStack = locateGravitatedStack(world, worldCoordinate);
        if (gravitatedStack.getStack().stream()
                .noneMatch(block1 -> StringUtils.equals(block.getBlockInfo().getId(), block1.getBlockInfo().getId()))) {
            gravitatedStack.getStack().push(block);
            block.getWorldCoordinate().getCoordinate()
                    .setZ(BigDecimal.valueOf(gravitatedStack.getMaxAltitude().intValue()));
            Map<Integer, Structure> structureMap = worldService.getStructureMap();
            BigDecimal height = structureMap.getOrDefault(block.getBlockInfo().getCode(), new Structure())
                    .getShape().getRadius().getZ();
            gravitatedStack.setMaxAltitude(block.getWorldCoordinate().getCoordinate().getZ().add(height));
        }
    }

    private void removeBlockFromGravitatedStack(final GameWorld world, Block block) {
        WorldCoordinate worldCoordinate = block.getWorldCoordinate();
        GravitatedStack gravitatedStack = locateGravitatedStack(world, worldCoordinate);
        Stack<Block> tempStack = new Stack<>();
        Block topBlock = null;
        Map<Integer, Structure> structureMap = worldService.getStructureMap();
        BigDecimal height = structureMap.getOrDefault(block.getBlockInfo().getCode(), new Structure())
                .getShape().getRadius().getZ();
        while (!gravitatedStack.getStack().empty()) {
            topBlock = gravitatedStack.getStack().pop();
            if (StringUtils.equals(block.getBlockInfo().getId(), topBlock.getBlockInfo().getId())) {
                gravitatedStack.setMaxAltitude(gravitatedStack.getMaxAltitude().subtract(height));
                break;
            } else {
                tempStack.push(topBlock);
            }
        }
        while (!tempStack.empty()) {
            gravitatedStack.getStack().push(tempStack.pop());
        }
        // Update creatures altitude
//        world.getCreatureMap().values().stream()
//                .filter(block1 -> BlockUtil.checkBlockTypeGravity(block1.getBlockInfo().getType()))
//                .filter(block1 -> block1.getWorldCoordinate().getRegionNo() == worldCoordinate.getRegionNo())
//                .filter(block1 -> block1.getWorldCoordinate().getSceneCoordinate() == worldCoordinate.getSceneCoordinate())
//                .filter(block1 -> BlockUtil.convertCoordinate2ClosestIntegerCoordinate(block1.getWorldCoordinate())
//                        .equals(BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate)))
//                .forEach(block1 -> updateBlockAltitude(world, block1));
        // Update blocks altitude
//        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
//        region.getScenes().get(worldCoordinate.getSceneCoordinate()).getBlocks().values().stream()
//                .filter(block1 -> block1.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_FLOOR)
//                .filter(block1 -> block1.getBlockInfo().getType() != BlockConstants.BLOCK_TYPE_WALL)
//                .filter(block1 -> BlockUtil.checkBlockTypeGravity(block1.getBlockInfo().getType()))
//                .filter(block1 -> BlockUtil.convertCoordinate2ClosestIntegerCoordinate(block1.getWorldCoordinate())
//                        .equals(BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate)))
//                .forEach(block1 -> updateBlockAltitude(world, block1));
    }
}
