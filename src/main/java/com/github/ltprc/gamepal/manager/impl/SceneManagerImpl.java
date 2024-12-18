package com.github.ltprc.gamepal.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.MovementManager;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.BagInfo;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.block.Block;
import com.github.ltprc.gamepal.model.map.block.BlockInfo;
import com.github.ltprc.gamepal.model.map.block.MovementInfo;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.service.WorldService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SceneManagerImpl implements SceneManager {

    private static final Log logger = LogFactory.getLog(SceneManagerImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private MovementManager movementManager;

    @Override
    public Region generateRegion(int regionNo) {
        Region region = new Region();
        region.setRegionNo(regionNo);
        region.setType(GamePalConstants.REGION_TYPE_ISLAND);
        region.setName("Auto Region " + region.getRegionNo());
        region.setWidth(GamePalConstants.SCENE_DEFAULT_WIDTH);
        region.setHeight(GamePalConstants.SCENE_DEFAULT_HEIGHT);
        region.setRadius(GamePalConstants.REGION_RADIUS_DEFAULT);
        initializeRegionTerrainMap(region);
        return region;
    }

    private void initializeRegionTerrainMap(Region region) {
        switch (region.getType()) {
            case GamePalConstants.REGION_TYPE_ISLAND:
                initializeRegionTerrainMapIsland(region);
                break;
        }
    }

    private void initializeRegionTerrainMapIsland(Region region) {
        Random random = new Random();
        Grid grid = new Grid(region.getRadius() * 2 + 1);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseStage(grid, noiseGenerator, 3, 0.1f);
        noiseStage(grid, noiseGenerator, 2, 0.3f);
        noiseStage(grid, noiseGenerator, 1, 0.6f);

        // Set temp altitudeMap
        Map<IntegerCoordinate, Double> altitudeMap = region.getAltitudeMap();
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                float gridVal = grid.get(i + region.getRadius(), j + region.getRadius());
                altitudeMap.put(sceneCoordinate, calculateIslandAltitude(region.getRadius(), sceneCoordinate) + gridVal);
            }
        }
        // Set terrainMap
        Map<IntegerCoordinate, Integer> terrainMap = region.getTerrainMap();
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, region.getRadius());
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_NOTHING);
            sceneCoordinate = new IntegerCoordinate(i, -region.getRadius());
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_NOTHING);
            sceneCoordinate = new IntegerCoordinate(region.getRadius(), i);
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_NOTHING);
            sceneCoordinate = new IntegerCoordinate(-region.getRadius(), i);
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_NOTHING);
        }
        for (int i = - region.getRadius() + 1; i < region.getRadius(); i++) {
            IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, region.getRadius() - 1);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(i, - region.getRadius() + 1);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(region.getRadius() - 1, i);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(- region.getRadius() + 1, i);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockConstants.BLOCK_CODE_WATER);
        }
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                int blockCode;
                double d = random.nextDouble();
                if (altitudeMap.get(sceneCoordinate) >= 0.65D) {
                    blockCode = BlockConstants.BLOCK_CODE_SNOW;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.6D) {
                    blockCode = BlockConstants.BLOCK_CODE_ROUGH;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.4D) {
                    if (d >= 0.75D) {
                        blockCode = BlockConstants.BLOCK_CODE_DIRT;
                    } else {
                        blockCode = BlockConstants.BLOCK_CODE_GRASS;
                    }
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                } else {
                    if (d >= 0.8D) {
                        blockCode = BlockConstants.BLOCK_CODE_WATER;
                    } else if (d >= 0.6D) {
                        blockCode = BlockConstants.BLOCK_CODE_SWAMP;
                    } else if (d >= 0.4D) {
                        blockCode = BlockConstants.BLOCK_CODE_SAND;
                    } else if (d >= 0.2D) {
                        blockCode = BlockConstants.BLOCK_CODE_LAVA;
                    } else {
                        blockCode = BlockConstants.BLOCK_CODE_SUBTERRANEAN;
                    }
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                }
            }
        }
    }

    private static void noiseStage(final Grid grid, final NoiseGenerator noiseGenerator, final int radius,
                                   final float modifier) {
        noiseGenerator.setRadius(radius);
        noiseGenerator.setModifier(modifier);
        // Seed ensures randomness, can be saved if you feel the need to
        // generate the same map in the future.
        noiseGenerator.setSeed(Generators.rollSeed());
        noiseGenerator.generate(grid);
    }

    private static double calculateIslandAltitude(final int radius, final IntegerCoordinate coordinate) {
        double ratio = Math.max(Math.abs(coordinate.getX()) / (double) radius,
                Math.abs(coordinate.getY()) / (double) radius);
        double rst;
        if (ratio < 0.5D) {
            rst = 0D;
        } else if (ratio < 0.9D) {
            rst = (ratio - 0.5D) * (-1D) / 0.4D;
        } else {
            rst = -1D;
        }
        return rst;
    }

    private static void defineScene(final Region region, Map<IntegerCoordinate, Double> altitudeMap,
                                    final IntegerCoordinate sceneCoordinate, final Double minAltitude,
                                    final Double maxAltitude, final int blockCode) {
        if (region.getTerrainMap().containsKey(sceneCoordinate)) {
            return;
        }
        if (Math.abs(sceneCoordinate.getX()) > region.getRadius()
                || Math.abs(sceneCoordinate.getY()) > region.getRadius()) {
            return;
        }
        if (!altitudeMap.containsKey(sceneCoordinate)) {
            return;
        }
        if (null != minAltitude && altitudeMap.get(sceneCoordinate) < minAltitude) {
            return;
        }
        if (null != maxAltitude && altitudeMap.get(sceneCoordinate) > maxAltitude) {
            return;
        }
        region.getTerrainMap().put(sceneCoordinate, blockCode);
        defineScene(region, altitudeMap, new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY()),
                minAltitude, maxAltitude, blockCode);
        defineScene(region, altitudeMap, new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY()),
                minAltitude, maxAltitude, blockCode);
        defineScene(region, altitudeMap, new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1),
                minAltitude, maxAltitude, blockCode);
        defineScene(region, altitudeMap, new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() - 1),
                minAltitude, maxAltitude, blockCode);
    }

    @Override
    public void fillScene(final GameWorld world, final Region region, final IntegerCoordinate sceneCoordinate) {
        if (region.getScenes().containsKey(sceneCoordinate)) {
            return;
        }
        Scene scene = new Scene();
        scene.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        scene.setName("Auto Scene (" + scene.getSceneCoordinate().getX() + "," + scene.getSceneCoordinate().getY()
                + ")");
        scene.setBlocks(new ConcurrentHashMap<>());
        if (Math.abs(sceneCoordinate.getX()) > region.getRadius()
                || Math.abs(sceneCoordinate.getY()) > region.getRadius() ) {
            logger.error(ErrorUtil.ERROR_1031);
            return;
        }
        int terrainCode = region.getTerrainMap().getOrDefault(sceneCoordinate, BlockConstants.BLOCK_CODE_NOTHING);
        region.getScenes().put(sceneCoordinate, scene);
        fillSceneTemplate(world, region, scene, terrainCode);
        if (terrainCode == BlockConstants.BLOCK_CODE_NOTHING) {
            scene.getBlocks().values().forEach(block -> removeBlock(world, block, false));
        } else {
            addSceneAnimals(world, region, scene);
        }
    }

    private Scene fillSceneTemplate(GameWorld world, final Region region, final Scene scene, final int blockCode) {
        scene.setGird(new int[region.getWidth() + 1][region.getHeight() + 1]);
        for (int i = 0; i <= region.getWidth(); i++) {
            for (int j = 0; j <= region.getHeight(); j++) {
                scene.getGird()[i][j] = 1001;
            }
        }
        IntegerCoordinate sceneCoordinate = scene.getSceneCoordinate();
        Scene scene1;
        for (int l = 0; l <= region.getWidth(); l++) {
            for (int k = 0; k <= region.getHeight(); k++) {
                scene.getGird()[l][k] = blockCode;
            }
        }
        // Area 0,0
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() - 1));
        if (null != scene1) {
            scene.getGird()[0][0] = scene1.getGird()[region.getWidth()][region.getHeight()];
        } else {
            scene.getGird()[0][0] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() - 1), blockCode);
        }
        // Area 2,0
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() - 1));
        if (null != scene1) {
            scene.getGird()[region.getWidth()][0] = scene1.getGird()[0][region.getHeight()];
        } else {
            scene.getGird()[region.getWidth()][0] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() - 1), blockCode);
        }
        // Area 0,2
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() + 1));
        if (null != scene1) {
            scene.getGird()[0][region.getHeight()] = scene1.getGird()[region.getWidth()][0];
        } else {
            scene.getGird()[0][region.getHeight()] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY() + 1), blockCode);
        }
        // Area 2,2
        scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() + 1));
        if (null != scene1) {
            scene.getGird()[region.getWidth()][region.getHeight()] = scene1.getGird()[0][0];
        } else {
            scene.getGird()[region.getWidth()][region.getHeight()] = region.getTerrainMap()
                    .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY() + 1), blockCode);
        }
        for (int i = 1; i < region.getWidth(); i++) {
            // Area 1,0
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() - 1));
            if (null != scene1) {
                scene.getGird()[i][0] = scene1.getGird()[i][region.getHeight()];
            } else {
                scene.getGird()[i][0] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1), blockCode);
            }
            // Area 1,2
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1));
            if (null != scene1) {
                scene.getGird()[i][region.getHeight()] = scene1.getGird()[i][0];
            } else {
                scene.getGird()[i][region.getHeight()] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX(), sceneCoordinate.getY() + 1), blockCode);
            }
        }
        for (int i = 1; i < region.getHeight(); i++) {
            // Area 0,1
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY()));
            if (null != scene1) {
                scene.getGird()[i][region.getHeight()] = scene1.getGird()[region.getHeight()][i];
            } else {
                scene.getGird()[i][region.getHeight()] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() - 1, sceneCoordinate.getY()), blockCode);
            }
            // Area 2,1
            scene1 = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY()));
            if (null != scene1) {
                scene.getGird()[region.getHeight()][i] = scene1.getGird()[0][i];
            } else {
                scene.getGird()[region.getHeight()][i] = region.getTerrainMap()
                        .getOrDefault(new IntegerCoordinate(sceneCoordinate.getX() + 1, sceneCoordinate.getY()), blockCode);
            }
        }
        // Pollute from 4 sides
        polluteBlockCode(region, scene, blockCode);
        addSceneObjects(world, region, scene);
        return scene;
    }

    private void polluteBlockCode(final Region region, final Scene scene, final int defaultBlockCode) {
        Random random = new Random();
        for (int l = 1; l < region.getWidth(); l++) {
            for (int k = 1; k < region.getHeight(); k++) {
                int upCode = scene.getGird()[l][0];
                int leftCode = scene.getGird()[0][k];
                int rightCode = scene.getGird()[region.getWidth()][k];
                int downCode = scene.getGird()[l][region.getHeight()];
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
                    scene.getGird()[l][k] = upCode;
                    continue;
                } else {
                    val -= upWeight;
                }
                if (val < leftWeight) {
                    scene.getGird()[l][k] = leftCode;
                    continue;
                } else {
                    val -= rightWeight;
                }
                if (val < rightWeight) {
                    scene.getGird()[l][k] = rightCode;
                    continue;
                } else {
                    val -= leftWeight;
                }
                if (val < downWeight) {
                    scene.getGird()[l][k] = downCode;
                    continue;
                } else {
                    val -= downWeight;
                }
                scene.getGird()[l][k] = defaultBlockCode;
            }
        }
    }

    private void addSceneObjects(GameWorld world, RegionInfo regionInfo, Scene scene) {
        Random random = new Random();
        for (int i = 0; i < regionInfo.getWidth(); i++) {
            for (int j = 0; j < regionInfo.getHeight(); j++) {
                switch (random.nextInt(4)) {
                    case 0:
                        int upleftBlockCode = scene.getGird()[i][j];
                        addSceneObject(world, regionInfo, scene, upleftBlockCode, BigDecimal.valueOf(i),
                                BigDecimal.valueOf(j));
                        break;
                    case 1:
                        int uprightBlockCode = scene.getGird()[i + 1][j];
                        addSceneObject(world, regionInfo, scene, uprightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j));
                        break;
                    case 2:
                        int downleftBlockCode = scene.getGird()[i][j + 1];
                        addSceneObject(world, regionInfo, scene, downleftBlockCode, BigDecimal.valueOf(i),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    case 3:
                        int downrightBlockCode = scene.getGird()[i + 1][j + 1];
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
        Random random = new Random();
        Coordinate coordinate = new Coordinate(x.add(BigDecimal.valueOf(random.nextDouble() / 2)),
                y.add(BigDecimal.valueOf(random.nextDouble() / 2)));
        WorldCoordinate worldCoordinate = new WorldCoordinate(regionInfo.getRegionNo(), scene.getSceneCoordinate(),
                coordinate);
        Map<Integer, Integer> weightMap = new LinkedHashMap<>();
        weightMap.put(BlockConstants.BLOCK_CODE_NOTHING, 2000);
        switch (blockCode) {
            case BlockConstants.BLOCK_CODE_DIRT:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_PINE, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_OAK, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_PINE, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_OAK, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_RAFFLESIA, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_STUMP, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_MOSSY_STUMP, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_HOLLOW_TRUNK, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_FLOWER_BUSH, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BUSH, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_3, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_3, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_1, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_2, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_1, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_2, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_3, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_4, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockConstants.ROCK_INDEX_1, 1);
                weightMap.put(BlockConstants.ROCK_INDEX_2, 1);
                break;
            case BlockConstants.BLOCK_CODE_SAND:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_PINE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_OAK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_PINE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_OAK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_PALM, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_RAFFLESIA, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_STUMP, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_MOSSY_STUMP, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_HOLLOW_TRUNK, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_FLOWER_BUSH, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BUSH, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_3, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_3, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_1, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_2, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_3, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_4, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_1, 50);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_2, 50);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_3, 50);
                weightMap.put(BlockConstants.ROCK_INDEX_1, 0);
                weightMap.put(BlockConstants.ROCK_INDEX_2, 0);
                break;
            case BlockConstants.BLOCK_CODE_GRASS:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_PINE, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_OAK, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_PINE, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_OAK, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_RAFFLESIA, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_STUMP, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_MOSSY_STUMP, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_HOLLOW_TRUNK, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_FLOWER_BUSH, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_BUSH, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_1, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_2, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_3, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_1, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_2, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_3, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_1, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_2, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_1, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_2, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_3, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_4, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockConstants.ROCK_INDEX_1, 10);
                weightMap.put(BlockConstants.ROCK_INDEX_2, 10);
                break;
            case BlockConstants.BLOCK_CODE_SWAMP:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_PINE, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_OAK, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_PINE, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_OAK, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_RAFFLESIA, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_STUMP, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_MOSSY_STUMP, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_HOLLOW_TRUNK, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_FLOWER_BUSH, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_BUSH, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_1, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_2, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_3, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_1, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_2, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_3, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_1, 50);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_2, 50);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_1, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_2, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_3, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_4, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockConstants.ROCK_INDEX_1, 10);
                weightMap.put(BlockConstants.ROCK_INDEX_2, 10);
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_PINE, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_OAK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_PINE, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_OAK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_RAFFLESIA, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_STUMP, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_MOSSY_STUMP, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_HOLLOW_TRUNK, 1);
                weightMap.put(BlockConstants.PLANT_INDEX_FLOWER_BUSH, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_BUSH, 20);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_1, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_2, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_3, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_1, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_2, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_3, 2);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_3, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_4, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_3, 5);
                weightMap.put(BlockConstants.ROCK_INDEX_1, 50);
                weightMap.put(BlockConstants.ROCK_INDEX_2, 50);
                break;
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_PINE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_OAK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_PINE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_OAK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_RAFFLESIA, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_STUMP, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_MOSSY_STUMP, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_HOLLOW_TRUNK, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_FLOWER_BUSH, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_BUSH, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_SMALL_FLOWER_3, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_1, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_2, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_FLOWER_3, 5);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_1, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_MUSHROOM_2, 100);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_1, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_2, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_3, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_GRASS_4, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockConstants.ROCK_INDEX_1, 100);
                weightMap.put(BlockConstants.ROCK_INDEX_2, 100);
                break;
            case BlockConstants.BLOCK_CODE_LAVA:
                weightMap.put(BlockConstants.PLANT_INDEX_BIG_WITHERED_TREE, 10);
                weightMap.put(BlockConstants.PLANT_INDEX_WITHERED_TREE, 10);
                break;
            case BlockConstants.BLOCK_CODE_SNOW:
            case BlockConstants.BLOCK_CODE_WATER:
            case BlockConstants.BLOCK_CODE_NOTHING:
            default:
                break;
        }
        int randomInt = random.nextInt(weightMap.values().stream().mapToInt(Integer::intValue).sum());
        List<Map.Entry<Integer, Integer>> weightList = new ArrayList<>(weightMap.entrySet());
        for (int i = 0; i < weightList.size() && randomInt >= 0; i++) {
            if (randomInt < weightList.get(i).getValue()
                    && weightList.get(i).getKey() != BlockConstants.BLOCK_CODE_NOTHING) {
                addSceneBlock(world, weightList.get(i).getKey(), worldCoordinate);
                break;
            }
            randomInt -= weightList.get(i).getValue();
        }
    }

    private void addSceneAnimals(GameWorld world, RegionInfo regionInfo, Scene scene) {
        Random random = new Random();
        for (int i = 0; i < regionInfo.getWidth(); i++) {
            for (int j = 0; j < regionInfo.getHeight(); j++) {
                switch (random.nextInt(4)) {
                    case 0:
                        int upleftBlockCode = scene.getGird()[i][j];
                        addSceneAnimal(world, regionInfo, scene, upleftBlockCode, BigDecimal.valueOf(i),
                                BigDecimal.valueOf(j));
                        break;
                    case 1:
                        int uprightBlockCode = scene.getGird()[i + 1][j];
                        addSceneAnimal(world, regionInfo, scene, uprightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j));
                        break;
                    case 2:
                        int downleftBlockCode = scene.getGird()[i][j + 1];
                        addSceneAnimal(world, regionInfo, scene, downleftBlockCode, BigDecimal.valueOf(i),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    case 3:
                        int downrightBlockCode = scene.getGird()[i + 1][j + 1];
                        addSceneAnimal(world, regionInfo, scene, downrightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void addSceneAnimal(GameWorld world, RegionInfo regionInfo, Scene scene, int blockCode, BigDecimal x,
                                BigDecimal y) {
        Random random = new Random();
        Map<Integer, Integer> weightMap = new LinkedHashMap<>();
        weightMap.put(BlockConstants.BLOCK_CODE_NOTHING, 10000);
        switch (blockCode) {
            case BlockConstants.BLOCK_CODE_DIRT:
                weightMap.put(CreatureConstants.SKIN_COLOR_DOG, 10);
                break;
            case BlockConstants.BLOCK_CODE_GRASS:
                weightMap.put(CreatureConstants.SKIN_COLOR_MONKEY, 2);
                weightMap.put(CreatureConstants.SKIN_COLOR_CHICKEN, 20);
                weightMap.put(CreatureConstants.SKIN_COLOR_BUFFALO, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_SHEEP, 20);
                weightMap.put(CreatureConstants.SKIN_COLOR_CAT, 5);
                weightMap.put(CreatureConstants.SKIN_COLOR_HORSE, 10);
                break;
            case BlockConstants.BLOCK_CODE_SNOW:
                weightMap.put(CreatureConstants.SKIN_COLOR_POLAR_BEAR, 10);
                break;
            case BlockConstants.BLOCK_CODE_SWAMP:
                weightMap.put(CreatureConstants.SKIN_COLOR_FROG, 50);
                break;
            case BlockConstants.BLOCK_CODE_ROUGH:
                weightMap.put(CreatureConstants.SKIN_COLOR_FOX, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_WOLF, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_TIGER, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_BOAR, 10);
                break;
            case BlockConstants.BLOCK_CODE_SUBTERRANEAN:
                weightMap.put(CreatureConstants.SKIN_COLOR_RACOON, 5);
                break;
            case BlockConstants.BLOCK_CODE_SAND:
            case BlockConstants.BLOCK_CODE_LAVA:
            case BlockConstants.BLOCK_CODE_WATER:
            case BlockConstants.BLOCK_CODE_NOTHING:
            default:
                break;
        }
        int randomInt = random.nextInt(weightMap.values().stream().mapToInt(Integer::intValue).sum());
        List<Map.Entry<Integer, Integer>> weightList = new ArrayList<>(weightMap.entrySet());
        for (int i = 0; i < weightList.size() && randomInt >= 0; i++) {
            if (randomInt < weightList.get(i).getValue()
                    && BlockConstants.BLOCK_CODE_NOTHING != weightList.get(i).getKey()) {
                String animalUserCode = UUID.randomUUID().toString();
                Block animal = npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                        CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                PlayerInfo playerInfo = world.getPlayerInfoMap().get(animal.getBlockInfo().getId());
                playerInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                int skinColor = weightList.get(i).getKey();
                playerInfo.setSkinColor(skinColor);
                WorldCoordinate worldCoordinate = new WorldCoordinate(regionInfo.getRegionNo(),
                        scene.getSceneCoordinate(), new Coordinate(x.add(BigDecimal.valueOf(random.nextDouble() / 2)),
                        y.add(BigDecimal.valueOf(random.nextDouble() / 2))));
                npcManager.putCreature(world, animalUserCode, worldCoordinate);
                break;
            }
            randomInt -= weightList.get(i).getValue();
        }
    }

    @Override
    public Queue<Block> collectBlocksByUserCode(final GameWorld world, final Block player, final int sceneScanRadius) {
        Queue<Block> rankingQueue = collectBlocksFromScenes(world, player, sceneScanRadius);
        rankingQueue.addAll(collectBlocksFromPlayerInfoMap(world, player, sceneScanRadius));
        return rankingQueue;
    }

    @Override
    public Queue<Block> collectBlocksFromScenes(final GameWorld world, final Block player, final int sceneScanRadius) {
        Queue<Block> rankingQueue = BlockUtil.createRankingQueue();
        IntegerCoordinate sceneCoordinate = player.getWorldCoordinate().getSceneCoordinate();
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        if (null == region) {
            logger.error(ErrorUtil.ERROR_1027);
            return rankingQueue;
        }
        // Collect blocks from SCENE_SCAN_RADIUS * SCENE_SCAN_RADIUS scenes 24/03/16
        for (int i = sceneCoordinate.getY() - sceneScanRadius;
             i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
            for (int j = sceneCoordinate.getX() - sceneScanRadius;
                 j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
                final IntegerCoordinate newSceneCoordinate = new IntegerCoordinate(j, i);
                Scene scene = region.getScenes().get(newSceneCoordinate);
                if (null == scene) {
                    continue;
                }
                if (!CollectionUtils.isEmpty(scene.getBlocks())) {
                    scene.getBlocks().values().forEach(block -> {
                        if (BlockUtil.checkPerceptionCondition(region, player,
                                player.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                ? world.getPlayerInfoMap().get(player.getBlockInfo().getId()).getPerceptionInfo()
                                : null, block)) {
                            Block newBlock = new Block(block);
                            rankingQueue.add(newBlock);
                        }
                    });
                }
            }
        }
        return rankingQueue;
    }

    @Override
    public Queue<Block> collectBlocksFromPlayerInfoMap(final GameWorld world, final Block player, final int sceneScanRadius) {
        Queue<Block> rankingQueue = BlockUtil.createRankingQueue();
        Map<String, Block> creatureMap = world.getCreatureMap();
        Region region = world.getRegionMap().get(player.getWorldCoordinate().getRegionNo());
        // Collect detected playerInfos
        creatureMap.values().stream()
                // playerInfos contains running players or NPC 24/03/25
                .filter(player1 -> playerService.validateActiveness(world, player1.getBlockInfo().getId()))
                .filter(player1 -> SkillUtil.isSceneDetected(player, player1.getWorldCoordinate(), sceneScanRadius))
                .forEach(player1 -> {
                    Block newBlock = new Block(player1);
                    if (BlockUtil.checkPerceptionCondition(region, player,
                            player.getBlockInfo().getType() == BlockConstants.BLOCK_TYPE_PLAYER
                                    ? world.getPlayerInfoMap().get(player.getBlockInfo().getId()).getPerceptionInfo()
                                    : null, newBlock)) {
//                        BlockUtil.adjustCoordinate(newBlock.getWorldCoordinate().getCoordinate(),
//                                BlockUtil.getCoordinateRelation(player.getWorldCoordinate().getSceneCoordinate(),
//                                        player.getWorldCoordinate().getSceneCoordinate()),
//                                region.getHeight(), region.getWidth());
                        rankingQueue.add(newBlock);
                    }
                });
        return rankingQueue;
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
//        for (int i = 0; i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
//            for (int j = 0; j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
//
//                Scene scene = region.getScenes().get(new IntegerCoordinate(sceneCoordinate.getX() - sceneScanRadius + i, sceneCoordinate.getY() - sceneScanRadius + j));
//                for (int l = 0; l <= region.getWidth(); l++) {
//                    for (int k = 0; k <= region.getHeight(); k++) {
//                        int val = BlockConstants.BLOCK_CODE_NOTHING;
//                        if (null != scene && null != scene.getGird()) {
//                            val = scene.getGird()[l][k];
//                        }
//                        grids[l + (j - sceneCoordinate.getX() + sceneScanRadius) * region.getWidth()]
//                                [k + (i - sceneCoordinate.getY() + sceneScanRadius) * region.getHeight()] = val;
//                    }
//                }
//            }
//        }
        for (int i = sceneCoordinate.getY() - sceneScanRadius; i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
            for (int j = sceneCoordinate.getX() - sceneScanRadius; j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
                final IntegerCoordinate newSceneCoordinate = new IntegerCoordinate(j, i);
                Scene scene = region.getScenes().get(newSceneCoordinate);
                for (int l = 0; l <= region.getWidth(); l++) {
                    for (int k = 0; k <= region.getHeight(); k++) {
                        int val = BlockConstants.BLOCK_CODE_NOTHING;
                        if (null != scene && null != scene.getGird()) {
                            val = scene.getGird()[l][k];
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
    public JSONObject convertBlock2OldBlockInstance(final GameWorld world, final String userCode, final Block block,
                                                    final boolean useWorldCoordinate) {
        JSONObject rst = new JSONObject();
        rst.putAll(JSON.parseObject(JSON.toJSONString(block.getBlockInfo())));
        if (useWorldCoordinate) {
            rst.putAll(JSON.parseObject(JSON.toJSONString(block.getWorldCoordinate())));
        } else {
            Region region = world.getRegionMap().get(block.getWorldCoordinate().getRegionNo());
            Block from = world.getCreatureMap().get(userCode);
            Coordinate coordinate = new Coordinate(block.getWorldCoordinate().getCoordinate());
            BlockUtil.adjustCoordinate(coordinate, BlockUtil.getCoordinateRelation(from.getWorldCoordinate().getSceneCoordinate(),
                    block.getWorldCoordinate().getSceneCoordinate()), region.getHeight(), region.getWidth());
            rst.putAll(JSON.parseObject(JSON.toJSONString(coordinate)));
        }
        rst.putAll(JSON.parseObject(JSON.toJSONString(block.getMovementInfo())));
        rst.put("code", block.getBlockInfo().getCode() + "-" + block.getMovementInfo().getFrame());
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_PLAYER:
                rst.putAll(JSON.parseObject(JSON.toJSONString(world.getPlayerInfoMap().get(block.getBlockInfo().getId()))));
                break;
            case BlockConstants.BLOCK_TYPE_DROP:
                if (!world.getDropMap().containsKey(block.getBlockInfo().getId())) {
                    return new JSONObject();
                }
                Map.Entry<String, Integer> entry = world.getDropMap().get(block.getBlockInfo().getId());
                rst.put("itemNo", entry.getKey());
                rst.put("amount", entry.getValue());
                break;
            case BlockConstants.BLOCK_TYPE_TELEPORT:
                if (!world.getTeleportMap().containsKey(block.getBlockInfo().getId())) {
                    return new JSONObject();
                }
                WorldCoordinate to = world.getTeleportMap().get(block.getBlockInfo().getId());
                rst.put("to", to);
                break;
            default:
                break;
        }
        return rst;
    }

    @Override
    public Block addLoadedBlock(GameWorld world, String code, Integer normalBlockType, WorldCoordinate worldCoordinate) {
        String id = UUID.randomUUID().toString();
        Structure structure;
        switch (normalBlockType) {
            case 2:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_ALL,
                        BlockConstants.STRUCTURE_LAYER_MIDDLE);
                break;
            case 3:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_TOP);
                break;
            case 1:
            default:
                structure = new Structure(BlockConstants.STRUCTURE_MATERIAL_NONE,
                        BlockConstants.STRUCTURE_LAYER_BOTTOM);
                break;
        }
        BlockInfo blockInfo = new BlockInfo(BlockConstants.BLOCK_TYPE_BUILDING, id, code, structure);
        MovementInfo movementInfo = new MovementInfo();
        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
        registerBlock(world, block);
        return block;
    }

    @Override
    public Block addSceneBlock(GameWorld world, int blockCode, WorldCoordinate worldCoordinate) {
        BlockInfo blockInfo = BlockUtil.generateSceneObjectBlockInfo(blockCode);
        MovementInfo movementInfo = new MovementInfo();
        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
        registerBlock(world, block);
        return block;
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
        BlockInfo blockInfo = BlockUtil.createBlockInfoByType(BlockConstants.BLOCK_TYPE_DROP);
        MovementInfo movementInfo = new MovementInfo();
        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
        registerBlock(world, block);
        if (null != drop) {
            world.getDropMap().put(block.getBlockInfo().getId(), drop);
            worldService.registerOnline(world, blockInfo);
        }
        return block;
    }

    @Override
    public Block addTeleportBlock(GameWorld world, final String code, WorldCoordinate worldCoordinate, WorldCoordinate to) {
        BlockInfo blockInfo = BlockUtil.createBlockInfoByType(BlockConstants.BLOCK_TYPE_TELEPORT);
        blockInfo.setCode(code);
        MovementInfo movementInfo = new MovementInfo();
        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
        registerBlock(world, block);
        if (null != to) {
            world.getTeleportMap().put(block.getBlockInfo().getId(), to);
        }
        return block;
    }

    @Override
    public Block addOtherBlock(final GameWorld world, final WorldCoordinate worldCoordinate, final BlockInfo blockInfo,
                               final MovementInfo movementInfo) {
        String id = UUID.randomUUID().toString();
        blockInfo.setId(id);
        Block block = new Block(worldCoordinate, blockInfo, movementInfo);
        registerBlock(world, block);
        if (blockInfo.getType() == BlockConstants.BLOCK_TYPE_CONTAINER) {
            BagInfo bagInfo = new BagInfo();
            bagInfo.setId(block.getBlockInfo().getId());
            world.getBagInfoMap().put(block.getBlockInfo().getId(), bagInfo);
            userService.addUserIntoWorldMap(id, world.getId());
        }
        return block;
    }

    @Transactional
    private Block registerBlock(GameWorld world, Block block) {
//        switch (eventCode) {
//            case GamePalConstants.EVENT_CODE_SHOOT_HIT:
//            case GamePalConstants.EVENT_CODE_SHOOT_ARROW:
//            case GamePalConstants.EVENT_CODE_SHOOT_SLUG:
//            case GamePalConstants.EVENT_CODE_SHOOT_MAGNUM:
//            case GamePalConstants.EVENT_CODE_SHOOT_ROCKET:
//            case GamePalConstants.EVENT_CODE_SHOOT_FIRE:
//            case GamePalConstants.EVENT_CODE_SHOOT_WATER:
//                break;
//            default:
//                registerBlock(world, block);
//                break;
//        }
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
                .filter(blocker -> BlockUtil.detectCollision(region, block, blocker))
                .noneMatch(blocker -> BlockUtil.checkMaterialCollision(block.getBlockInfo().getStructure().getMaterial(),
                        blocker.getBlockInfo().getStructure().getMaterial()));
    }

    @Override
    public void removeBlock(GameWorld world, Block block, boolean isDestroyed) {
        if (isDestroyed) {
            destroyBlock(world, block);
        }
        unregisterBlock(world, block);
    }

    @Transactional
    private void unregisterBlock(GameWorld world, Block block) {
        WorldCoordinate worldCoordinate = block.getWorldCoordinate();
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
        scene.getBlocks().remove(block.getBlockInfo().getId());
        world.getBlockMap().remove(block.getBlockInfo().getId());
        worldService.registerOffline(world, block.getBlockInfo());
        world.getSourceMap().remove(block.getBlockInfo().getId());
        switch (block.getBlockInfo().getType()) {
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
                world.getBagInfoMap().remove(block.getBlockInfo().getId());
                break;
            default:
                break;
        }
    }

    private void destroyBlock(GameWorld world, Block block) {
        Random random = new Random();
        Block drop = null;
        switch (block.getBlockInfo().getType()) {
            case BlockConstants.BLOCK_TYPE_BED:
            case BlockConstants.BLOCK_TYPE_DRESSER:
            case BlockConstants.BLOCK_TYPE_STORAGE:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_wood", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            case BlockConstants.BLOCK_TYPE_CONTAINER:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_wood", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                world.getBagInfoMap().get(block.getBlockInfo().getId()).getItems().entrySet()
                        .forEach(entry -> {
                            Block dropFromContainer = addDropBlock(world, block.getWorldCoordinate(), entry);
                            movementManager.speedUpBlock(world, dropFromContainer,
                                    BlockUtil.locateCoordinateWithDirectionAndDistance(new Coordinate(),
                                            BigDecimal.valueOf(random.nextDouble() * 360),
                                            GamePalConstants.DROP_THROW_RADIUS));
                        });
                break;
            case BlockConstants.BLOCK_TYPE_TOILET:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_ceramic", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            case BlockConstants.BLOCK_TYPE_COOKER:
            case BlockConstants.BLOCK_TYPE_BUILDING:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_concrete", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            case BlockConstants.BLOCK_TYPE_SINK:
            case BlockConstants.BLOCK_TYPE_WORKSHOP:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_TOOL:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_AMMO:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_OUTFIT:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_CHEM:
            case BlockConstants.BLOCK_TYPE_WORKSHOP_RECYCLE:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_steel", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            case BlockConstants.BLOCK_TYPE_SPEAKER:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_plastic", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            case BlockConstants.BLOCK_TYPE_TREE:
                for (int i = 0; i < 10 + random.nextInt(10); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_wood", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            case BlockConstants.BLOCK_TYPE_ROCK:
                for (int i = 0; i < 3 + random.nextInt(3); i++) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_concrete", 1));
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                int randomValue = random.nextInt(100);
                if (randomValue < 2) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_gold", 1 + random.nextInt(2)));
                } else if (randomValue < 5) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_silver", 1 + random.nextInt(2)));
                } else if (randomValue < 8) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_lead", 1 + random.nextInt(2)));
                } else if (randomValue < 12) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_asbestos", 1 + random.nextInt(2)));
                } else if (randomValue < 16) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_oil", 1 + random.nextInt(2)));
                } else if (randomValue < 20) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_crystal", 1 + random.nextInt(2)));
                } else if (randomValue < 25) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_aluminum", 1 + random.nextInt(2)));
                } else if (randomValue < 27) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_bone", 1 + random.nextInt(2)));
                } else if (randomValue < 35) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_copper", 1 + random.nextInt(2)));
                } else if (randomValue < 45) {
                    drop = addDropBlock(world, block.getWorldCoordinate(), new AbstractMap.SimpleEntry<>("m_steel", 1 + random.nextInt(2)));
                }
                if (null != drop) {
                    movementManager.speedUpBlock(world, drop, BlockUtil.locateCoordinateWithDirectionAndDistance(
                            new Coordinate(), BigDecimal.valueOf(random.nextDouble() * 360),
                            GamePalConstants.DROP_THROW_RADIUS));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public int getGridBlockCode(GameWorld world, WorldCoordinate worldCoordinate) {
        int code = BlockConstants.BLOCK_CODE_NOTHING;
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
        if (null != scene.getGird() && null != scene.getGird()[0]) {
            IntegerCoordinate gridCoordinate = BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate);
            code = scene.getGird()[gridCoordinate.getX()][gridCoordinate.getY()];
        }
        return code;
    }

    @Override
    public void setGridBlockCode(GameWorld world, WorldCoordinate worldCoordinate, int code) {
        Region region = world.getRegionMap().get(worldCoordinate.getRegionNo());
        Scene scene = region.getScenes().get(worldCoordinate.getSceneCoordinate());
        if (null != scene.getGird() && null != scene.getGird()[0]) {
            IntegerCoordinate gridCoordinate = BlockUtil.convertCoordinate2ClosestIntegerCoordinate(worldCoordinate);
            scene.getGird()[gridCoordinate.getX()][gridCoordinate.getY()] = code;
            IntegerCoordinate nearbySceneCoordinate = new IntegerCoordinate(worldCoordinate.getSceneCoordinate());
            if (gridCoordinate.getX() == 0) {
                nearbySceneCoordinate.setX(nearbySceneCoordinate.getX() - 1);
                gridCoordinate.setX(scene.getGird()[0].length - 1);
            }
            if (gridCoordinate.getX() == scene.getGird()[0].length - 1) {
                nearbySceneCoordinate.setX(nearbySceneCoordinate.getX() + 1);
                gridCoordinate.setX(0);
            }
            if (gridCoordinate.getY() == 0) {
                nearbySceneCoordinate.setY(nearbySceneCoordinate.getY() - 1);
                gridCoordinate.setY(scene.getGird().length - 1);
            }
            if (gridCoordinate.getY() == scene.getGird().length - 1) {
                nearbySceneCoordinate.setY(nearbySceneCoordinate.getY() + 1);
                gridCoordinate.setY(0);
            }
            scene = region.getScenes().get(nearbySceneCoordinate);
            if (null != scene && null != scene.getGird() && null != scene.getGird()[0]) {
                scene.getGird()[scene.getGird().length - 1][gridCoordinate.getY()] = code;
            }
        }
    }
}
