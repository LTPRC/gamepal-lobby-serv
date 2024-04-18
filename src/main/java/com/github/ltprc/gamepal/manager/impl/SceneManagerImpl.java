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
        noiseStage(grid, noiseGenerator, 3, 0.5f);
        noiseStage(grid, noiseGenerator, 2, 0.3f);
        noiseStage(grid, noiseGenerator, 1, 0.2f);

        // Generate temp altitudeMap
        Map<IntegerCoordinate, Double> altitudeMap = new HashMap<>();
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                float gridVal = grid.get(i + region.getRadius(), j + region.getRadius());
                altitudeMap.put(sceneCoordinate, calculateIslandAltitude(region.getRadius(), sceneCoordinate) * gridVal);
            }
        }
        // Generate and store terrainMap
        Map<IntegerCoordinate, Integer> terrainMap = region.getTerrainMap();
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, region.getRadius());
            defineScene(region, altitudeMap, sceneCoordinate, Double.MIN_VALUE, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(i, -region.getRadius());
            defineScene(region, altitudeMap, sceneCoordinate, Double.MIN_VALUE, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(region.getRadius(), i);
            defineScene(region, altitudeMap, sceneCoordinate, Double.MIN_VALUE, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
            sceneCoordinate = new IntegerCoordinate(-region.getRadius(), i);
            defineScene(region, altitudeMap, sceneCoordinate, Double.MIN_VALUE, 0D, BlockCodeConstants.BLOCK_CODE_WATER);
        }
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                int blockCode;
                double d = random.nextDouble();
                if (altitudeMap.get(sceneCoordinate) >= 0.75D) {
                    blockCode = BlockCodeConstants.BLOCK_CODE_SNOW;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), 0.75D, Double.MAX_VALUE, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0.5D) {
                    blockCode = BlockCodeConstants.BLOCK_CODE_ROUGH;
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), 0.5D, 0.75D, blockCode);
                } else if (altitudeMap.get(sceneCoordinate) >= 0D) {
                    if (d >= 0.5D) {
                        blockCode = BlockCodeConstants.BLOCK_CODE_GRASS;
                    } else {
                        blockCode = BlockCodeConstants.BLOCK_CODE_DIRT;
                    }
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), 0D, 0.5D, blockCode);
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
                    defineScene(region, altitudeMap, new IntegerCoordinate(i, j), Double.MIN_VALUE, 0D, blockCode);
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
        double ratio = (coordinate.getX() + coordinate.getY()) / ((double) radius * 2);
        double rst;
        if (ratio < 0.8D) {
            rst = 1D;
        } else if (ratio < 0.9D) {
            rst = (ratio - 0.8D) * (-2D) / 0.1D + 1D;
        } else {
            rst = -1D;
        }
        return rst;
    }

    private static void defineScene(final Region region, Map<IntegerCoordinate, Double> altitudeMap,
                                    final IntegerCoordinate sceneCoordinate, final double minAltitude,
                                    final double maxAltitude, final int blockCode) {
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
        if (altitudeMap.get(sceneCoordinate) < minAltitude || altitudeMap.get(sceneCoordinate) > maxAltitude) {
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

    private void calculateTerrainInSquare(Map<IntegerCoordinate, BigDecimal> altitudeMap, int x1, int y1, int x2,
                                          int y2) {
        if (Math.abs(x1 - x2) <= 1 || Math.abs(y1 - y2) <= 1) {
            return;
        }
        if (!altitudeMap.containsKey(new IntegerCoordinate(x1, y1))
                || !altitudeMap.containsKey(new IntegerCoordinate(x1, y2))
                || !altitudeMap.containsKey(new IntegerCoordinate(x2, y1))
                || !altitudeMap.containsKey(new IntegerCoordinate(x2, y2))) {
            logger.error(ErrorUtil.ERROR_1035);
        }
        Random random = new Random();
        BigDecimal value11 = altitudeMap.get(new IntegerCoordinate(x1, y1));
        BigDecimal value12 = altitudeMap.get(new IntegerCoordinate(x1, y2));
        BigDecimal value21 = altitudeMap.get(new IntegerCoordinate(x2, y1));
        BigDecimal value22 = altitudeMap.get(new IntegerCoordinate(x2, y2));
        int x3 = (x1 + x2) / 2;
        int y3 = (y1 + y2) / 2;
        altitudeMap.put(new IntegerCoordinate(x3, y1),
                value11.add(value21).divide(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(random.nextDouble() * 2 - 1)));
        altitudeMap.put(new IntegerCoordinate(x3, y2),
                value12.add(value22).divide(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(random.nextDouble() * 2 - 1)));
        altitudeMap.put(new IntegerCoordinate(x1, y3),
                value11.add(value12).divide(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(random.nextDouble() * 2 - 1)));
        altitudeMap.put(new IntegerCoordinate(x2, y3),
                value21.add(value22).divide(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(random.nextDouble() * 2 - 1)));
        altitudeMap.put(new IntegerCoordinate(x3, y3),
                value11.add(value12).add(value21).add(value22).divide(BigDecimal.valueOf(4))
                        .add(BigDecimal.valueOf(random.nextDouble() * 2 - 1)));
        calculateTerrainInSquare(altitudeMap, x1, y1, x3, y3);
        calculateTerrainInSquare(altitudeMap, x3, y1, x2, y3);
        calculateTerrainInSquare(altitudeMap, x1, y3, x3, y2);
        calculateTerrainInSquare(altitudeMap, x3, y3, x2, y2);
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
        if (Math.abs(sceneCoordinate.getX()) == region.getRadius()
                || Math.abs(sceneCoordinate.getY()) == region.getRadius() ) {
            regionIndex = BlockCodeConstants.BLOCK_CODE_NOTHING;
        }
        switch (regionIndex) {
            case BlockCodeConstants.BLOCK_CODE_GRASS:
                fillSceneGrass(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_WATER:
                fillSceneOcean(region, scene);
                break;
            case BlockCodeConstants.BLOCK_CODE_DIRT:
            case BlockCodeConstants.BLOCK_CODE_SAND:
            case BlockCodeConstants.BLOCK_CODE_SNOW:
            case BlockCodeConstants.BLOCK_CODE_SWAMP:
            case BlockCodeConstants.BLOCK_CODE_ROUGH:
            case BlockCodeConstants.BLOCK_CODE_SUBTERRANEAN:
            case BlockCodeConstants.BLOCK_CODE_LAVA:
                fillSceneTemplate(region, scene, String.valueOf(regionIndex));
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

    private Scene fillSceneNothing(final RegionInfo regionInfo, final Scene scene) {
        // Fill floor
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

    private Scene fillSceneOcean(final RegionInfo regionInfo, final Scene scene) {
        // Fill floor
        scene.setBlocks(new ArrayList<>());
        for (int k = 0; k < regionInfo.getHeight(); k++) {
            for (int l = 0; l < regionInfo.getWidth(); l++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND, null,
                        String.valueOf(BlockCodeConstants.BLOCK_CODE_WATER),
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_GROUND),
                        new Coordinate(BigDecimal.valueOf(l), BigDecimal.valueOf(k)));
                scene.getBlocks().add(block);
            }
        }

        return scene;
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

    private Scene fillSceneGrass(final RegionInfo regionInfo, final Scene scene) {
        Random random = new Random();
        for (int k = 0; k < regionInfo.getHeight(); k++) {
            for (int l = 0; l < regionInfo.getWidth(); l++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND, null,
                        String.valueOf(BlockCodeConstants.BLOCK_CODE_GRASS),
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_GROUND),
                        new Coordinate(BigDecimal.valueOf(l), BigDecimal.valueOf(k)));
                scene.getBlocks().add(block);
            }
        }

        // 松树
        for (int j = 0; j < random.nextInt(10); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-0-0",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                    new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D))),
                    new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 橡树
        for (int j = 0; j < random.nextInt(10); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-2-0",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                    new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D))),
                            new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 死树
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-4-0",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                    new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D))),
                            new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 细松树
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-0-2",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                    new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D))),
                            new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 细橡树
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-1-2",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                    new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D))),
                            new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 细死树
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-2-2",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.ZERO),
                                    new Coordinate(BigDecimal.valueOf(0.1D), BigDecimal.valueOf(0.1D))),
                            new Coordinate(BigDecimal.ONE, BigDecimal.valueOf(2))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 大石头
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-0-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 树桩1
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-1-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 树桩2
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-2-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 空心树干
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-3-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 灌木丛1
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-4-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW, GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 灌木丛2
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-5-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW, GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
            scene.getBlocks().add(block);
        }
        // 其他装饰
        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < random.nextInt(3); j++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND_DECORATION, null, "f-" + (i % 8) + "-" + (i / 8 + 5),
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        new Coordinate(BigDecimal.valueOf(random.nextDouble() * regionInfo.getWidth()),
                                BigDecimal.valueOf(random.nextDouble() * regionInfo.getHeight())));
                scene.getBlocks().add(block);
            }
        }
        return scene;
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
