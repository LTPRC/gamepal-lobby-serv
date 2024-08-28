package com.github.ltprc.gamepal.manager.impl;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;
import com.github.ltprc.gamepal.config.BlockCodeConstants;
import com.github.ltprc.gamepal.config.CreatureConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.NpcManager;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.creature.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.model.map.world.WorldCoordinate;
import com.github.ltprc.gamepal.service.PlayerService;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import com.github.ltprc.gamepal.util.SkillUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
public class SceneManagerImpl implements SceneManager {

    private static final Log logger = LogFactory.getLog(SceneManagerImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private NpcManager npcManager;

    @Autowired
    private PlayerService playerService;

    @Override
    public Region generateRegion(int regionNo) {
        Region region = new Region();
        region.setRegionNo(regionNo);
        region.setType(BlockCodeConstants.REGION_TYPE_ISLAND);
        region.setName("Auto Region " + region.getRegionNo());
        region.setWidth(GamePalConstants.SCENE_DEFAULT_WIDTH);
        region.setHeight(GamePalConstants.SCENE_DEFAULT_HEIGHT);
        region.setRadius(BlockCodeConstants.REGION_RADIUS_DEFAULT);
        initializeRegionTerrainMap(region);
        return region;
    }

    private void initializeRegionTerrainMap(Region region) {
        switch (region.getType()) {
            case BlockCodeConstants.REGION_TYPE_ISLAND:
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
            region.getTerrainMap().put(sceneCoordinate, BlockCodeConstants.BLOCK_CODE_NOTHING);
            sceneCoordinate = new IntegerCoordinate(i, -region.getRadius());
            region.getTerrainMap().put(sceneCoordinate, BlockCodeConstants.BLOCK_CODE_NOTHING);
            sceneCoordinate = new IntegerCoordinate(region.getRadius(), i);
            region.getTerrainMap().put(sceneCoordinate, BlockCodeConstants.BLOCK_CODE_NOTHING);
            sceneCoordinate = new IntegerCoordinate(-region.getRadius(), i);
            region.getTerrainMap().put(sceneCoordinate, BlockCodeConstants.BLOCK_CODE_NOTHING);
        }
        for (int i = - region.getRadius() + 1; i < region.getRadius(); i++) {
            IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, region.getRadius() - 1);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(i, - region.getRadius() + 1);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(region.getRadius() - 1, i);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(- region.getRadius() + 1, i);
            region.getAltitudeMap().put(sceneCoordinate, -1D);
            defineScene(region, altitudeMap, sceneCoordinate, null, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
        }
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                int blockCode;
                double d = random.nextDouble();
                if (altitudeMap.get(sceneCoordinate) >= 0.65D) {
                    blockCode = BlockCodeConstants.BLOCK_CODE_SNOW;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.6D) {
                    blockCode = BlockCodeConstants.BLOCK_CODE_ROUGH;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.4D) {
                    if (d >= 0.75D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_DIRT;
                    } else {
                        blockCode = BlockCodeConstants.BLOCK_CODE_GRASS;
                    }
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), altitudeMap.get(sceneCoordinate) - 0.05D,
                            altitudeMap.get(sceneCoordinate) + 0.05D, blockCode);
                } else {
                    if (d >= 0.8D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_WATER;
                    } else if (d >= 0.6D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_SWAMP;
                    } else if (d >= 0.4D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_SAND;
                    } else if (d >= 0.2D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_LAVA;
                    } else {
                        blockCode = BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN;
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
        scene.setBlocks(new ArrayList<>());
        if (Math.abs(sceneCoordinate.getX()) > region.getRadius()
                || Math.abs(sceneCoordinate.getY()) > region.getRadius() ) {
            logger.error(ErrorUtil.ERROR_1031);
            return;
        }
        int regionIndex = region.getTerrainMap().getOrDefault(sceneCoordinate, BlockCodeConstants.BLOCK_CODE_NOTHING);
        fillSceneTemplate(region, scene, regionIndex);
        if (regionIndex == BlockCodeConstants.BLOCK_CODE_NOTHING) {
            scene.getBlocks().forEach(block -> block.getStructure().setMaterial(GamePalConstants.STRUCTURE_MATERIAL_SOLID));
        } else {
            // Add animals 24/06/19
            addSceneAnimals(world, region, scene);
        }
        scene.setEvents(new CopyOnWriteArrayList<>());
        region.getScenes().put(sceneCoordinate, scene);
    }

    private Scene fillSceneTemplate(final Region region, final Scene scene, final int blockCode) {
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
        addSceneObjects(region, scene);
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

    @Deprecated
    private Scene fillSceneDirt(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_DIRT);
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 橡树
        addSceneObject(region, scene, 10, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 细橡树
        addSceneObject(region, scene, 10, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        return scene;
    }

    @Deprecated
    private Scene fillSceneSand(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_SAND);
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 棕榈树
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        // 仙人掌
        for (int i = 0; i < 3; i++) {
            addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                    BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-" + i + "-8", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                    GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        }
        return scene;
    }

    @Deprecated
    private Scene fillSceneGrass(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_GRASS);
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 松树
        addSceneObject(region, scene, 10, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 橡树
        addSceneObject(region, scene, 10, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 死树
        addSceneObject(region, scene, 2, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 细松树
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        // 细橡树
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        // 细死树
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)));
        // 大石头
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, null);
        // 小石头
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_BOTTOM, roundShape, null);
        // 树桩1
        addSceneObject(region, scene, 2, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-4", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, null);
        // 树桩2
        addSceneObject(region, scene, 2, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-4", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, null);
        // 空心树干
        addSceneObject(region, scene, 2, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-4", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, null);
        // 灌木丛1
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        // 灌木丛2
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        // 鲜花
        for (int i = 0; i < 6; i++) {
            addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                    BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-" + i + "-5", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                    GamePalConstants.STRUCTURE_LAYER_BOTTOM, null, null);
        }
        return scene;
    }

    @Deprecated
    private Scene fillSceneSnow(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_SNOW);
        return scene;
    }

    @Deprecated
    private Scene fillSceneSwamp(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_SWAMP);
        // 霸王花
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_BOTTOM, null, null);
        // 杂草
        for (int i = 0; i < 4; i++) {
            addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                    BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-" + i + "-7", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                    GamePalConstants.STRUCTURE_LAYER_BOTTOM, null, null);
        }
        return scene;
    }

    @Deprecated
    private Scene fillSceneRough(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_ROUGH);
        // 灌木丛1
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        // 灌木丛2
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        return scene;
    }

    @Deprecated
    private Scene fillSceneSubterranean(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN);
        // 霸王花
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_BOTTOM, null, null);
        // 蘑菇1
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-6", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        // 蘑菇2
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-6", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, null, null);
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)));
        // 大石头
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, null);
        // 小石头
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_BOTTOM, roundShape, null);
        return scene;
    }

    @Deprecated
    private Scene fillSceneLava(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_LAVA);
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)));
        // 大石头
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                GamePalConstants.STRUCTURE_LAYER_MIDDLE, roundShape, null);
        // 小石头
        addSceneObject(region, scene, 5, GamePalConstants.BLOCK_TYPE_NORMAL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                GamePalConstants.STRUCTURE_LAYER_BOTTOM, roundShape, null);
        return scene;
    }

    @Deprecated
    private Scene fillSceneOcean(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_WATER);
        return scene;
    }

    @Deprecated
    private Scene fillSceneNothing(final Region region, final Scene scene) {
        fillSceneTemplate(region, scene, BlockCodeConstants.BLOCK_CODE_NOTHING);
        scene.getBlocks().forEach(block -> block.getStructure().setMaterial(GamePalConstants.STRUCTURE_MATERIAL_SOLID));
        return scene;
    }

    @Deprecated
    private void addSceneObject(RegionInfo regionInfo, Scene scene, int maxAmount, int blockType, String blockCode,
                                int material, int layer, Shape shape, Coordinate imageSize) {
        Random random = new Random();
        Structure structure;
        if (null != shape && null != imageSize) {
            structure = new Structure(material, layer, shape, imageSize);
        } else if (null != shape) {
            structure = new Structure(material, layer, shape);
        } else {
            structure = new Structure(material, layer);
        }
        for (int j = 0; j < random.nextInt(maxAmount); j++) {
            Block block = new Block(blockType, null, blockCode, structure,
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
    }

    private void addSceneObjects(RegionInfo regionInfo, Scene scene) {
        Random random = new Random();
        for (int i = 0; i < regionInfo.getWidth(); i++) {
            for (int j = 0; j < regionInfo.getHeight(); j++) {
                switch (random.nextInt(4)) {
                    case 0:
                        int upleftBlockCode = scene.getGird()[i][j];
                        addSceneObject(regionInfo, scene, upleftBlockCode, BigDecimal.valueOf(i),
                                BigDecimal.valueOf(j));
                        break;
                    case 1:
                        int uprightBlockCode = scene.getGird()[i + 1][j];
                        addSceneObject(regionInfo, scene, uprightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j));
                        break;
                    case 2:
                        int downleftBlockCode = scene.getGird()[i][j + 1];
                        addSceneObject(regionInfo, scene, downleftBlockCode, BigDecimal.valueOf(i),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    case 3:
                        int downrightBlockCode = scene.getGird()[i + 1][j + 1];
                        addSceneObject(regionInfo, scene, downrightBlockCode, BigDecimal.valueOf(i + 0.5D),
                                BigDecimal.valueOf(j + 0.5D));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void addSceneObject(RegionInfo regionInfo, Scene scene, int blockCode, BigDecimal x, BigDecimal y) {
        Random random = new Random();
        Coordinate coordinate = new Coordinate(x.add(BigDecimal.valueOf(random.nextDouble() / 2)),
                y.add(BigDecimal.valueOf(random.nextDouble() / 2)));
        Map<Integer, Integer> weightMap = new LinkedHashMap<>();
        weightMap.put(BlockCodeConstants.BLOCK_CODE_NOTHING, 2000);
        switch (blockCode) {
            case BlockCodeConstants.BLOCK_CODE_DIRT:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_PINE, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_OAK, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PINE, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_OAK, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_RAFFLESIA, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_STUMP, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BUSH, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_1, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_2, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_1, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_2, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_3, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_4, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_1, 1);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_2, 1);
                break;
            case BlockCodeConstants.BLOCK_CODE_SAND:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_PINE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_OAK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PINE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_OAK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PALM, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_RAFFLESIA, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_STUMP, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BUSH, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_1, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_2, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_3, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_4, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_1, 50);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_2, 50);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_3, 50);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_1, 0);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_2, 0);
                break;
            case BlockCodeConstants.BLOCK_CODE_GRASS:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_PINE, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_OAK, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PINE, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_OAK, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_RAFFLESIA, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_STUMP, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BUSH, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_1, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_2, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_1, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_2, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_3, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_4, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_1, 10);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_2, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_SWAMP:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_PINE, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_OAK, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PINE, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_OAK, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_RAFFLESIA, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_STUMP, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BUSH, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_1, 50);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_2, 50);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_1, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_2, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_3, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_4, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_1, 10);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_2, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_ROUGH:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_PINE, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_OAK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PINE, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_OAK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_RAFFLESIA, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_STUMP, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK, 1);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BUSH, 20);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3, 2);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_3, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_4, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_3, 5);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_1, 50);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_2, 50);
                break;
            case BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_PINE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_OAK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PINE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_OAK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_PALM, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_RAFFLESIA, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_STUMP, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BUSH, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3, 5);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_1, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_MUSHROOM_2, 100);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_1, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_2, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_3, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_GRASS_4, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_1, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_2, 0);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_CACTUS_3, 0);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_1, 100);
                weightMap.put(BlockCodeConstants.ROCK_INDEX_2, 100);
                break;
            case BlockCodeConstants.BLOCK_CODE_LAVA:
                weightMap.put(BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE, 10);
                weightMap.put(BlockCodeConstants.PLANT_INDEX_WITHERED_TREE, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_SNOW:
            case BlockCodeConstants.BLOCK_CODE_WATER:
            case BlockCodeConstants.BLOCK_CODE_NOTHING:
            default:
                break;
        }
        int randomInt = random.nextInt(weightMap.values().stream().mapToInt(Integer::intValue).sum());
        List<Map.Entry<Integer, Integer>> weightList = new ArrayList<>(weightMap.entrySet());
        for (int i = 0; i < weightList.size() && randomInt >= 0; i++) {
            if (randomInt < weightList.get(i).getValue()) {
                addSceneObjectByCode(regionInfo, scene, weightList.get(i).getKey(), coordinate);
                break;
            }
            randomInt -= weightList.get(i).getValue();
        }
    }

    private void addSceneObjectByCode(RegionInfo regionInfo, Scene scene, int blockCode, Coordinate coordinate) {
        Block block = null;
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        switch (blockCode) {
            case BlockCodeConstants.PLANT_INDEX_BIG_PINE:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-0",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_BIG_OAK:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-0",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_BIG_WITHERED_TREE:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-0",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_PINE:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-2",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_OAK:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-2",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_WITHERED_TREE:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-2",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_PALM:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-2",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape,
                                new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_RAFFLESIA:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-4",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_STUMP:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-4",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_MOSSY_STUMP:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-4",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_HOLLOW_TRUNK:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-4",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_FLOWER_BUSH:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-4",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_BUSH:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-4",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_1:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-5",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_2:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-5",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_SMALL_FLOWER_3:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-5",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_1:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-5",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_2:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-5",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_BIG_FLOWER_3:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-5",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_MUSHROOM_1:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-6",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_MUSHROOM_2:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-6",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_GRASS_1:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-7",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_GRASS_2:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-7",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_GRASS_3:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-7",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_GRASS_4:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-7",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_CACTUS_1:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-8",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_CACTUS_2:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-8",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                        coordinate);
                break;
            case BlockCodeConstants.PLANT_INDEX_CACTUS_3:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-8",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                        coordinate);
                break;
            case BlockCodeConstants.ROCK_INDEX_1:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                                roundShape),
                        coordinate);
                break;
            case BlockCodeConstants.ROCK_INDEX_2:
                block = new Block(GamePalConstants.BLOCK_TYPE_NORMAL, null,
                        BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM,
                                roundShape),
                        coordinate);
                break;
            default:
                break;
        }
        if (null != block) {
            scene.getBlocks().add(block);
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
        weightMap.put(BlockCodeConstants.BLOCK_CODE_NOTHING, 10000);
        switch (blockCode) {
            case BlockCodeConstants.BLOCK_CODE_DIRT:
                weightMap.put(CreatureConstants.SKIN_COLOR_DOG, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_GRASS:
                weightMap.put(CreatureConstants.SKIN_COLOR_MONKEY, 2);
                weightMap.put(CreatureConstants.SKIN_COLOR_CHICKEN, 20);
                weightMap.put(CreatureConstants.SKIN_COLOR_BUFFALO, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_SHEEP, 20);
                weightMap.put(CreatureConstants.SKIN_COLOR_CAT, 5);
                weightMap.put(CreatureConstants.SKIN_COLOR_HORSE, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_SNOW:
                weightMap.put(CreatureConstants.SKIN_COLOR_POLAR_BEAR, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_SWAMP:
                weightMap.put(CreatureConstants.SKIN_COLOR_FROG, 50);
                break;
            case BlockCodeConstants.BLOCK_CODE_ROUGH:
                weightMap.put(CreatureConstants.SKIN_COLOR_FOX, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_WOLF, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_TIGER, 10);
                weightMap.put(CreatureConstants.SKIN_COLOR_BOAR, 10);
                break;
            case BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN:
                weightMap.put(CreatureConstants.SKIN_COLOR_RACOON, 5);
                break;
            case BlockCodeConstants.BLOCK_CODE_SAND:
            case BlockCodeConstants.BLOCK_CODE_LAVA:
            case BlockCodeConstants.BLOCK_CODE_WATER:
            case BlockCodeConstants.BLOCK_CODE_NOTHING:
            default:
                break;
        }
        int randomInt = random.nextInt(weightMap.values().stream().mapToInt(Integer::intValue).sum());
        List<Map.Entry<Integer, Integer>> weightList = new ArrayList<>(weightMap.entrySet());
        for (int i = 0; i < weightList.size() && randomInt >= 0; i++) {
            if (randomInt < weightList.get(i).getValue()
                    && BlockCodeConstants.BLOCK_CODE_NOTHING != weightList.get(i).getKey()) {
                String animalUserCode = UUID.randomUUID().toString();
                PlayerInfo animalInfo =
                        npcManager.createCreature(world, CreatureConstants.PLAYER_TYPE_NPC,
                                CreatureConstants.CREATURE_TYPE_ANIMAL, animalUserCode);
                animalInfo.setPlayerStatus(GamePalConstants.PLAYER_STATUS_RUNNING);
                int skinColor = weightList.get(i).getKey();
                animalInfo.setSkinColor(skinColor);
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
    public Queue<Block> collectBlocksByUserCode(String userCode, final int sceneScanRadius) {
        Queue<Block> rankingQueue = collectBlocksFromScenes(userCode, sceneScanRadius);
        rankingQueue.addAll(collectBlocksFromPlayerInfoMap(userCode, sceneScanRadius));
        return rankingQueue;
    }

    @Override
    public Queue<Block> collectBlocksFromScenes(String userCode, final int sceneScanRadius) {
        Queue<Block> rankingQueue = BlockUtil.createRankingQueue();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        IntegerCoordinate sceneCoordinate = playerInfo.getSceneCoordinate();
        Region region = world.getRegionMap().get(playerInfo.getRegionNo());
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
                    scene.getBlocks().forEach(block -> {
                        Block newBlock;
                        switch (block.getType()) {
                            case GamePalConstants.BLOCK_TYPE_DROP:
                                newBlock = new Drop((Drop) block);
                                break;
                            case GamePalConstants.BLOCK_TYPE_TELEPORT:
                                newBlock = new Teleport((Teleport) block);
                                break;
                            default:
                                newBlock = new Block(block);
                                break;
                        }
                        BlockUtil.adjustCoordinate(newBlock,
                                BlockUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                                        newSceneCoordinate), region.getHeight(), region.getWidth());
                        if (BlockUtil.checkPerceptionCondition(playerInfo.getPerceptionInfo(),
                                playerInfo.getFaceDirection(), playerInfo.getCoordinate(), newBlock)) {
                            rankingQueue.add(newBlock);
                        }
                    });
                }
                // Generate blocks from scene events 24/02/16
                if (!CollectionUtils.isEmpty(scene.getEvents())) {
                    new ArrayList<>(scene.getEvents()).forEach(event -> {
                        Block newBlock = BlockUtil.convertEvent2Block(event);
                        BlockUtil.adjustCoordinate(newBlock,
                                BlockUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                                        newSceneCoordinate), region.getHeight(), region.getWidth());
                        if (BlockUtil.checkPerceptionCondition(playerInfo.getPerceptionInfo(),
                                playerInfo.getFaceDirection(), playerInfo.getCoordinate(), newBlock)) {
                            rankingQueue.add(newBlock);
                        }
                    });
                }
            }
        }
        return rankingQueue;
    }

    @Override
    public Queue<Block> collectBlocksFromPlayerInfoMap(String userCode, final int sceneScanRadius) {
        Queue<Block> rankingQueue = BlockUtil.createRankingQueue();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        Region region = world.getRegionMap().get(playerInfo.getRegionNo());
        // Collect detected playerInfos
        playerInfoMap.values().stream()
                // playerInfos contains running players or NPC 24/03/25
                .filter(playerInfo1 -> playerService.validateActiveness(world, playerInfo1))
                .filter(playerInfo1 -> SkillUtil.isBlockDetected(playerInfo, playerInfo1, sceneScanRadius))
                .forEach(playerInfo1 -> {
                    Block block = BlockUtil.convertWorldBlock2Block(region, playerInfo1, false);
                    BlockUtil.adjustCoordinate(block, BlockUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                            playerInfo1.getSceneCoordinate()), region.getHeight(), region.getWidth());
                    if (BlockUtil.checkPerceptionCondition(playerInfo.getPerceptionInfo(),
                            playerInfo.getFaceDirection(), playerInfo.getCoordinate(), block)) {
                        rankingQueue.add(block);
                    }
                });
        return rankingQueue;
    }

    @Override
    public int[][] collectGridsByUserCode(String userCode, int sceneScanRadius) {
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        IntegerCoordinate sceneCoordinate = playerInfo.getSceneCoordinate();
        Region region = world.getRegionMap().get(playerInfo.getRegionNo());
        int[][] grids = new int[region.getWidth() * (sceneScanRadius * 2 + 1) + 1]
                [region.getHeight() * (sceneScanRadius * 2 + 1) + 1];
        for (int i = sceneCoordinate.getY() - sceneScanRadius; i <= sceneCoordinate.getY() + sceneScanRadius; i++) {
            for (int j = sceneCoordinate.getX() - sceneScanRadius; j <= sceneCoordinate.getX() + sceneScanRadius; j++) {
                final IntegerCoordinate newSceneCoordinate = new IntegerCoordinate(j, i);
                Scene scene = region.getScenes().get(newSceneCoordinate);
                for (int l = 0; l <= region.getWidth(); l++) {
                    for (int k = 0; k <= region.getHeight(); k++) {
                        int val = BlockCodeConstants.BLOCK_CODE_NOTHING;
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
}
