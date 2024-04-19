package com.github.ltprc.gamepal.manager.impl;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;
import com.github.ltprc.gamepal.config.BlockCodeConstants;
import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.BlockUtil;
import com.github.ltprc.gamepal.util.ErrorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Component
public class SceneManagerImpl implements SceneManager {

    private static final Log logger = LogFactory.getLog(SceneManagerImpl.class);

    @Autowired
    private UserService userService;

    @Override
    public Region generateRegion(int regionNo) {
        Region region = new Region();
        region.setRegionNo(regionNo);
        region.setName("Auto Region " + region.getRegionNo());
        region.setWidth(GamePalConstants.SCENE_DEFAULT_WIDTH);
        region.setHeight(GamePalConstants.SCENE_DEFAULT_HEIGHT);
        region.setRadius(GamePalConstants.SCENE_SCAN_MAX_RADIUS);
        region.setScenes(new HashMap<>());
        region.setTerrainMap(new HashMap<>());
        region.setAltitudeMap(new HashMap<>());
        initializeRegionTerrainMap(region);
        return region;
    }

    private void initializeRegionTerrainMap(Region region) {
        initializeRegionTerrainMapIsland(region);
    }

    private void initializeRegionTerrainMapIsland(Region region) {
        Random random = new Random();
        Grid grid = new Grid(region.getRadius() * 2 + 1);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseStage(grid, noiseGenerator, 3, 0.1f);
        noiseStage(grid, noiseGenerator, 2, 0.4f);
        noiseStage(grid, noiseGenerator, 1, 0.5f);

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
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), 0.65D, null, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.55D) {
                    blockCode = BlockCodeConstants.BLOCK_CODE_ROUGH;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), 0.55D, 0.65D, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.3D) {
                    if (d >= 0.75D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_DIRT;
                    } else {
                        blockCode = BlockCodeConstants.BLOCK_CODE_GRASS;
                    }
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), 0.3D, 0.55D, blockCode);
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
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), null, 0.3D, blockCode);
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
    public void fillScene(final Region region, final IntegerCoordinate sceneCoordinate) {
        Scene scene = new Scene();
        scene.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        scene.setName("Auto Scene (" + scene.getSceneCoordinate().getX() + "," + scene.getSceneCoordinate().getY()
                + ")");
        scene.setBlocks(new ArrayList<>());
        if (Math.abs(sceneCoordinate.getX()) > region.getRadius()
                || Math.abs(sceneCoordinate.getY()) > region.getRadius() ) {
            logger.error(ErrorUtil.ERROR_1035);
            return;
        }
        int regionIndex = region.getTerrainMap().getOrDefault(sceneCoordinate, BlockCodeConstants.BLOCK_CODE_NOTHING);
//        if (Math.abs(sceneCoordinate.getX()) == region.getRadius()
//                || Math.abs(sceneCoordinate.getY()) == region.getRadius() ) {
//            regionIndex = BlockCodeConstants.BLOCK_CODE_NOTHING;
//        }
        switch (regionIndex) {
            case BlockCodeConstants.BLOCK_CODE_DIRT:
                fillSceneDirt(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_SAND:
                fillSceneSand(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_GRASS:
                fillSceneGrass(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_SNOW:
                fillSceneSnow(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_SWAMP:
                fillSceneSwamp(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_ROUGH:
                fillSceneRough(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN:
                fillSceneSubterranean(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_LAVA:
                fillSceneLava(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_WATER:
                fillSceneOcean(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_NOTHING:
            default:
                fillSceneNothing(region, scene);
                break;
        }
        // Add events
        scene.setEvents(new CopyOnWriteArrayList<>());

        region.getScenes().put(sceneCoordinate, scene);
    }

    private Scene fillSceneTemplate(final RegionInfo regionInfo, final Scene scene, final String blockCode) {
        for (int k = 0; k < regionInfo.getHeight(); k++) {
            for (int l = 0; l < regionInfo.getWidth(); l++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND, null, blockCode,
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_GROUND),
                        new Coordinate(BigDecimal.valueOf(l), BigDecimal.valueOf(k)));
                scene.getBlocks().add(block);
            }
        }
        return scene;
    }

    private Scene fillSceneDirt(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_DIRT));
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 橡树
        addSceneObject(regionInfo, scene, 10, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 细橡树
        addSceneObject(regionInfo, scene, 10, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        return scene;
    }

    private Scene fillSceneSand(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_SAND));
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 棕榈树
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        // 仙人掌
        for (int i = 0; i < 3; i++) {
            addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                    BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-" + i + "-8", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                    null, null);
        }
        return scene;
    }

    private Scene fillSceneGrass(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_GRASS));
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 松树
        addSceneObject(regionInfo, scene, 10, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 橡树
        addSceneObject(regionInfo, scene, 10, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 死树
        addSceneObject(regionInfo, scene, 2, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        // 细松树
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        // 细橡树
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        // 细死树
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-2", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2)));
        roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 大石头
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, null);
        // 小石头
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                roundShape, null);
        // 树桩1
        addSceneObject(regionInfo, scene, 2, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-4", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, null);
        // 树桩2
        addSceneObject(regionInfo, scene, 2, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-2-4", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, null);
        // 空心树干
        addSceneObject(regionInfo, scene, 2, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-3-4", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, null);
        // 霸王花
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        // 灌木丛1
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        // 灌木丛2
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        // 鲜花
        for (int i = 0; i < 6; i++) {
            addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_GROUND_DECORATION,
                    BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-" + i + "-5", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                    null, null);
        }
        return scene;
    }

    private Scene fillSceneSnow(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_SNOW));
        return scene;
    }

    private Scene fillSceneSwamp(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_SWAMP));
        // 杂草
        for (int i = 0; i < 4; i++) {
            addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_GROUND_DECORATION,
                    BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-" + i + "-7", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                    null, null);
        }
        return scene;
    }

    private Scene fillSceneRough(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_ROUGH));
        // 灌木丛1
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-4-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        // 灌木丛2
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-5-4", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        return scene;
    }

    private Scene fillSceneSubterranean(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN));
        // 蘑菇1
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-0-6", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        // 蘑菇2
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_PLANTS + "-1-6", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                null, null);
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 大石头
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, null);
        // 小石头
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                roundShape, null);
        return scene;
    }

    private Scene fillSceneLava(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_LAVA));
        Shape roundShape = new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D)));
        // 大石头
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-0", GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                roundShape, null);
        // 小石头
        addSceneObject(regionInfo, scene, 5, GamePalConstants.BLOCK_TYPE_WALL,
                BlockCodeConstants.BLOCK_CODE_PREFIX_ROCKS + "-0-1", GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                roundShape, null);
        return scene;
    }

    private Scene fillSceneOcean(final RegionInfo regionInfo, final Scene scene) {
        fillSceneTemplate(regionInfo, scene, String.valueOf(BlockCodeConstants.BLOCK_CODE_WATER));
        return scene;
    }

    private Scene fillSceneNothing(final RegionInfo regionInfo, final Scene scene) {
        for (int k = 0; k < regionInfo.getHeight(); k++) {
            for (int l = 0; l < regionInfo.getWidth(); l++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND, null,
                        String.valueOf(BlockCodeConstants.BLOCK_CODE_NOTHING),
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_GROUND),
                        new Coordinate(BigDecimal.valueOf(l), BigDecimal.valueOf(k)));
                scene.getBlocks().add(block);
            }
        }
        return scene;
    }

    private void addSceneObject(RegionInfo regionInfo, Scene scene, int maxAmount, int blockType, String blockCode,
                                int structureMaterial, Shape shape, Coordinate imageSize) {
        Random random = new Random();
        Structure structure;
        if (null != shape && null != imageSize) {
            structure = new Structure(structureMaterial, GamePalConstants.STRUCTURE_LAYER_MIDDLE, shape, imageSize);
        } else if (null != shape) {
            structure = new Structure(structureMaterial, GamePalConstants.STRUCTURE_LAYER_MIDDLE, shape);
        } else {
            structure = new Structure(structureMaterial, GamePalConstants.STRUCTURE_LAYER_MIDDLE);
        }
        for (int j = 0; j < random.nextInt(maxAmount); j++) {
            Block block = new Block(blockType, null, blockCode, structure,
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth() - 1D),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight() - 1D)));
            scene.getBlocks().add(block);
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
                    scene.getBlocks().stream()
                            .forEach(block -> {
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
                                // Save communication cost 24/04/09
                                if (BlockUtil.calculateDistance(newBlock, playerInfo.getCoordinate())
                                        .compareTo(BlockUtil.calculateViewRadius(world.getWorldTime())) <= 0) {
                                    rankingQueue.add(newBlock);
                                }
                    });
                }
                // Generate blocks from scene events 24/02/16
                if (!CollectionUtils.isEmpty(scene.getEvents())) {
                    scene.getEvents().stream().collect(Collectors.toList())
                            .forEach(event -> {
                                Block newBlock = BlockUtil.convertEvent2Block(event);
                                BlockUtil.adjustCoordinate(newBlock,
                                        BlockUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                                                newSceneCoordinate), region.getHeight(), region.getWidth());
                                rankingQueue.add(newBlock);
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
        IntegerCoordinate sceneCoordinate = playerInfo.getSceneCoordinate();
        Region region = world.getRegionMap().get(playerInfo.getRegionNo());
        // Collect detected playerInfos
        playerInfoMap.entrySet().stream()
                // playerInfos contains running players or NPC 24/03/25
                .filter(entry -> world.getOnlineMap().containsKey(entry.getKey()))
                .filter(entry -> entry.getValue().getPlayerStatus() == GamePalConstants.PLAYER_STATUS_RUNNING)
                // Detected
                .filter(entry -> entry.getValue().getRegionNo() == playerInfo.getRegionNo())
                .filter(entry -> {
                    IntegerCoordinate integerCoordinate
                            = BlockUtil.getCoordinateRelation(sceneCoordinate, entry.getValue().getSceneCoordinate());
                    return Math.abs(integerCoordinate.getX()) <= sceneScanRadius
                            && Math.abs(integerCoordinate.getY()) <= sceneScanRadius;
                })
                .forEach(entry -> {
                    Block block = BlockUtil.convertWorldBlock2Block(region, entry.getValue(), false);
                    BlockUtil.adjustCoordinate(block, BlockUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                            entry.getValue().getSceneCoordinate()), region.getHeight(), region.getWidth());
                    rankingQueue.add(block);
                });
        return rankingQueue;
    }
}
