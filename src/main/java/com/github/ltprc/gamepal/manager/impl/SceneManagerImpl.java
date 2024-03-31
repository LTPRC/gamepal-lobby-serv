package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.factory.BlockFactory;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.PlayerInfo;
import com.github.ltprc.gamepal.model.map.*;
import com.github.ltprc.gamepal.model.map.world.GameWorld;
import com.github.ltprc.gamepal.service.UserService;
import com.github.ltprc.gamepal.util.PlayerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;


@Component
public class SceneManagerImpl implements SceneManager {

    @Autowired
    private UserService userService;

    @Autowired
    private BlockFactory blockFactory;

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
                Block block = new Block();
                block.setX(BigDecimal.valueOf(l));
                block.setY(BigDecimal.valueOf(k));
                block.setType(GamePalConstants.BLOCK_TYPE_BLOCKED_GROUND);
                block.setCode("1001");
                scene.getBlocks().add(block);
            }
        }

        // Add extra blocks

        // Add events
        scene.setEvents(new ArrayList<>());

        region.getScenes().put(sceneCoordinate, scene);
    }

    private void fillSceneGrassland(final Region region, final IntegerCoordinate sceneCoordinate) {
        Scene scene = new Scene();
        scene.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        scene.setName("Auto Scene (" + scene.getSceneCoordinate().getX() + "," + scene.getSceneCoordinate().getY()
                + ")");

        // Fill floor
        scene.setBlocks(new ArrayList<>());
        for (int k = 0; k < region.getHeight(); k++) {
            for (int l = 0; l < region.getWidth(); l++) {
                Block block = new Block();
                block.setX(BigDecimal.valueOf(l));
                block.setY(BigDecimal.valueOf(k));
                block.setType(GamePalConstants.BLOCK_TYPE_GROUND);
                block.setCode("1010");
                scene.getBlocks().add(block);
            }
        }

        // Add extra blocks
        Random random = new Random();
        int treeAmount = random.nextInt(20);
        for (int i = 0; i < treeAmount; i++) {
            TreeBlock treeBlock = new TreeBlock();
            treeBlock.setX(BigDecimal.valueOf(random.nextDouble() * region.getWidth()));
            treeBlock.setY(BigDecimal.valueOf(random.nextDouble() * region.getHeight()));
            treeBlock.setType(GamePalConstants.BLOCK_TYPE_TREE);
            treeBlock.setTreeType(GamePalConstants.TREE_TYPE_PINE);
            treeBlock.setTreeHeight(2);
            treeBlock.setRadius(GamePalConstants.PLAYER_RADIUS);
            scene.getBlocks().add(treeBlock);
        }

        // Add events
        scene.setEvents(new ArrayList<>());

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
        Queue<Block> rankingQueue = blockFactory.createRankingQueue();
        GameWorld world = userService.getWorldByUserCode(userCode);
        Map<String, PlayerInfo> playerInfoMap = world.getPlayerInfoMap();
        PlayerInfo playerInfo = playerInfoMap.get(userCode);
        IntegerCoordinate sceneCoordinate = playerInfo.getSceneCoordinate();
        Region region = world.getRegionMap().get(playerInfo.getRegionNo());
        // Collect blocks from SCENE_SCAN_RADIUS * SCENE_SCAN_RADIUS scenes 24/03/16
        if (null == sceneCoordinate){
            System.out.println("100");
        }
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
                    scene.getBlocks().stream().forEach(block -> {
                        Block newBlock = PlayerUtil.copyBlock(block);
                        PlayerUtil.adjustCoordinate(newBlock,
                                PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(), newSceneCoordinate),
                                region.getHeight(), region.getWidth());
                        rankingQueue.add(newBlock);
                    });
                }
                // Generate blocks from scene events 24/02/16
                if (!CollectionUtils.isEmpty(scene.getEvents())) {
                    scene.getEvents().stream().forEach(event -> {
                        Block newBlock = PlayerUtil.generateBlockByEvent(event);
                        PlayerUtil.adjustCoordinate(newBlock,
                                PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(), newSceneCoordinate),
                                region.getHeight(), region.getWidth());
                        rankingQueue.add(newBlock);
                    });
                }
            }
        }
        return rankingQueue;
    }

    @Override
    public Queue<Block> collectBlocksFromPlayerInfoMap(String userCode, final int sceneScanRadius) {
        Queue<Block> rankingQueue = blockFactory.createRankingQueue();
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
                            = PlayerUtil.getCoordinateRelation(sceneCoordinate, entry.getValue().getSceneCoordinate());
                    return Math.abs(integerCoordinate.getX()) <= sceneScanRadius
                            && Math.abs(integerCoordinate.getY()) <= sceneScanRadius;
                })
                .forEach(entry -> {
                    Block block = new Block();
                    block.setType(GamePalConstants.BLOCK_TYPE_PLAYER);
                    block.setId(entry.getValue().getId());
                    block.setCode(entry.getValue().getCode());
                    block.setY(entry.getValue().getCoordinate().getY());
                    block.setX(entry.getValue().getCoordinate().getX());
                    PlayerUtil.adjustCoordinate(block, PlayerUtil.getCoordinateRelation(playerInfo.getSceneCoordinate(),
                            entry.getValue().getSceneCoordinate()), region.getHeight(), region.getWidth());
                    rankingQueue.add(block);
                });
        return rankingQueue;
    }
}
