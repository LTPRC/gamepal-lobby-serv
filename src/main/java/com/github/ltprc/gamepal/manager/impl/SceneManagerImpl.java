package com.github.ltprc.gamepal.manager.impl;

import com.github.ltprc.gamepal.config.GamePalConstants;
import com.github.ltprc.gamepal.manager.SceneManager;
import com.github.ltprc.gamepal.model.map.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


@Component
public class SceneManagerImpl implements SceneManager {

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
}
