package com.github.ltprc.gamepal.factory;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.noise.NoiseGenerator;
import com.github.czyzby.noise4j.map.generator.util.Generators;
import com.github.ltprc.gamepal.config.BlockConstants;
import com.github.ltprc.gamepal.config.RegionConstants;
import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

public class RegionFactory {

    private static final Random random = new Random();

    private RegionFactory() {}

    public static Region generateRegion(int regionNo, int regionType, String name, int width, int height, int radius,
                                 BigDecimal altitude) {
        Region region = new Region();
        region.setRegionNo(regionNo);
        region.setType(regionType);
        region.setName(name);
        region.setWidth(width);
        region.setHeight(height);
        region.setRadius(radius);
        region.setAltitude(altitude);
        initializeRegionTerrainMap(region);
        return region;
    }

    private static void initializeRegionTerrainMap(Region region) {
        switch (region.getType()) {
            case RegionConstants.REGION_TYPE_ISLAND:
                initializeRegionTerrainMapIsland(region);
                break;
            default:
            case RegionConstants.REGION_TYPE_EMPTY:
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
                break;
        }
    }

    private static void initializeRegionTerrainMapIsland(Region region) {
        Grid grid = new Grid(region.getRadius() * 2 + 1);
        NoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseStage(grid, noiseGenerator, 3, 0.1f);
        noiseStage(grid, noiseGenerator, 2, 0.3f);
        noiseStage(grid, noiseGenerator, 1, 0.6f);

        // Set temp altitudeMap
        Map<IntegerCoordinate, BigDecimal> altitudeMap = region.getAltitudeMap();
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                float gridVal = grid.get(i + region.getRadius(), j + region.getRadius());
                altitudeMap.put(sceneCoordinate,
                        BigDecimal.valueOf(calculateIslandAltitude(region.getRadius(), sceneCoordinate) + gridVal));
            }
        }
        // Set terrainMap
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, region.getRadius());
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_BLACK);
            sceneCoordinate = new IntegerCoordinate(i, -region.getRadius());
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_BLACK);
            sceneCoordinate = new IntegerCoordinate(region.getRadius(), i);
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_BLACK);
            sceneCoordinate = new IntegerCoordinate(-region.getRadius(), i);
            region.getTerrainMap().put(sceneCoordinate, BlockConstants.BLOCK_CODE_BLACK);
        }
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                int blockCode;
                double d = random.nextDouble();
                if (altitudeMap.get(sceneCoordinate).compareTo(BigDecimal.valueOf(0.65D)) >= 0) {
                    blockCode = BlockConstants.BLOCK_CODE_SNOW;
                } else if (altitudeMap.get(sceneCoordinate).compareTo(BigDecimal.valueOf(0.6D)) >= 0) {
                    blockCode = BlockConstants.BLOCK_CODE_ROUGH;
                } else if (altitudeMap.get(sceneCoordinate).compareTo(BigDecimal.valueOf(0.4D)) >= 0) {
                    if (d >= 0.75D) {
                        blockCode = BlockConstants.BLOCK_CODE_DIRT;
                    } else {
                        blockCode = BlockConstants.BLOCK_CODE_GRASS;
                    }
                } else if (altitudeMap.get(sceneCoordinate).compareTo(BigDecimal.valueOf(0D)) >= 0) {
                    if (d >= 0.8D) {
                        blockCode = BlockConstants.BLOCK_CODE_WATER_SHALLOW;
                    } else if (d >= 0.6D) {
                        blockCode = BlockConstants.BLOCK_CODE_SWAMP;
                    } else if (d >= 0.4D) {
                        blockCode = BlockConstants.BLOCK_CODE_SAND;
                    } else if (d >= 0.2D) {
                        blockCode = BlockConstants.BLOCK_CODE_LAVA;
                    } else {
                        blockCode = BlockConstants.BLOCK_CODE_SUBTERRANEAN;
                    }
                } else if (altitudeMap.get(sceneCoordinate).compareTo(BigDecimal.valueOf(-0.1D)) >= 0) {
                    blockCode = BlockConstants.BLOCK_CODE_WATER_SHALLOW;
                } else if (altitudeMap.get(sceneCoordinate).compareTo(BigDecimal.valueOf(-0.2D)) >= 0) {
                    blockCode = BlockConstants.BLOCK_CODE_WATER_MEDIUM;
                } else {
                    blockCode = BlockConstants.BLOCK_CODE_WATER_DEEP;
                }
                SceneFactory.defineScene(region, altitudeMap, new IntegerCoordinate(i, j),
                        altitudeMap.get(sceneCoordinate).doubleValue() - 0.05D,
                        altitudeMap.get(sceneCoordinate).doubleValue() + 0.05D, blockCode);
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
}
