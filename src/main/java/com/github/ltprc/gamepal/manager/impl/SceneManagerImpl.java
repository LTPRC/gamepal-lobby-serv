package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.structure.Shape;
import com.github.ltprc.gamepal.model.map.structure.Structure;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.BlockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Component
public class SceneManagerImpl implements SceneManager {

    @Autowired
    private UserService userService;

    @Override
    public Region generateRegion(int regionNo) {
        Region region = new Region();
        region.setRegionNo(regionNo);
        region.setName("Auto Region " + region.getRegionNo());
        region.setWidth(GamePalConstants.SCENE_DEFAULT_WIDTH);
        region.setHeight(GamePalConstants.SCENE_DEFAULT_HEIGHT);
        region.setScenes(new HashMap<>());
        return region;
    }

    @Override
    public void fillScene(final Region region, final IntegerCoordinate sceneCoordinate, int regionIndex) {
        if (Math.abs(sceneCoordinate.getX()) == GamePalConstants.SCENE_SCAN_MAX_RADIUS
                || Math.abs(sceneCoordinate.getY()) == GamePalConstants.SCENE_SCAN_MAX_RADIUS ) {
            fillSceneNothing(region, sceneCoordinate);
            return;
        }
        switch (regionIndex) {
            case GamePalConstants.REGION_INDEX_GRASSLAND:
                fillSceneGrassland(region, sceneCoordinate);
                break;
            case GamePalConstants.REGION_INDEX_NOTHING:
            default:
                fillSceneNothing(region, sceneCoordinate);
                break;
        }
    }

    private void fillSceneNothing(final Region region, final IntegerCoordinate sceneCoordinate) {
        Scene scene = new Scene();
        scene.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        scene.setName("Auto Scene (" + scene.getSceneCoordinate().getX() + "," + scene.getSceneCoordinate().getY()
                + ")");

        // Fill floor
        scene.setBlocks(new ArrayList<>());
        for (int k = 0; k < region.getHeight(); k++) {
            for (int l = 0; l < region.getWidth(); l++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND, null, "1001",
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_GROUND),
                        new Coordinate(BigDecimal.valueOf(l), BigDecimal.valueOf(k)));
                scene.getBlocks().add(block);
            }
        }

        // Add extra blocks

        // Add events
        scene.setEvents(new CopyOnWriteArrayList<>());

        region.getScenes().put(sceneCoordinate, scene);
    }

    private void fillSceneGrassland(final Region region, final IntegerCoordinate sceneCoordinate) {
        Random random = new Random();
        Scene scene = new Scene();
        scene.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        scene.setName("Auto Scene (" + scene.getSceneCoordinate().getX() + "," + scene.getSceneCoordinate().getY()
                + ")");

        // Fill floor
        scene.setBlocks(new ArrayList<>());
        for (int k = 0; k < region.getHeight(); k++) {
            for (int l = 0; l < region.getWidth(); l++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND, null, "1010",
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
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
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
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
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
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
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
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
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
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
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
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 大石头
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-0-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 树桩1
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-1-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 树桩2
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-2-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 空心树干
        for (int j = 0; j < random.nextInt(2); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-3-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID, GamePalConstants.STRUCTURE_LAYER_MIDDLE,
                            new Shape(GamePalConstants.STRUCTURE_SHAPE_TYPE_ROUND,
                                    new Coordinate(BigDecimal.ZERO, BigDecimal.valueOf(-0.25D)),
                                    new Coordinate(BigDecimal.valueOf(0.25D), BigDecimal.valueOf(0.25D)))),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 灌木丛1
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-4-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW, GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 灌木丛2
        for (int j = 0; j < random.nextInt(5); j++) {
            Block block = new Block(GamePalConstants.BLOCK_TYPE_WALL, null, "f-5-4",
                    new Structure(GamePalConstants.STRUCTURE_MATERIAL_HOLLOW, GamePalConstants.STRUCTURE_LAYER_MIDDLE),
                    new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                            BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
            scene.getBlocks().add(block);
        }
        // 其他装饰
        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < random.nextInt(3); j++) {
                Block block = new Block(GamePalConstants.BLOCK_TYPE_GROUND_DECORATION, null, "f-" + (i % 8) + "-" + (i / 8 + 5),
                        new Structure(GamePalConstants.STRUCTURE_MATERIAL_SOLID,
                                GamePalConstants.STRUCTURE_LAYER_BOTTOM),
                        new Coordinate(BigDecimal.valueOf(random.nextDouble() * region.getWidth()),
                                BigDecimal.valueOf(random.nextDouble() * region.getHeight())));
                scene.getBlocks().add(block);
            }
        }

        // Add events
        scene.setEvents(new CopyOnWriteArrayList<>());

        region.getScenes().put(sceneCoordinate, scene);
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
                                        .compareTo(GamePalConstants.PLAYER_VIEW_RADIUS) <= 0) {
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
