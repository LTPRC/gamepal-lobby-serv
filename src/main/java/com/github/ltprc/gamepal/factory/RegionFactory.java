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
                BigDecimal gridVal = BigDecimal.valueOf(grid.get(i + region.getRadius(), j + region.getRadius()))
                        .add(calculateAltitude(region, sceneCoordinate));
                altitudeMap.put(sceneCoordinate, gridVal);
            }
        }
        // Define edges on terrainMap
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
        // Define scenes
        for (int i = - region.getRadius(); i <= region.getRadius(); i++) {
            for (int j = - region.getRadius(); j <= region.getRadius(); j++) {
                IntegerCoordinate sceneCoordinate = new IntegerCoordinate(i, j);
                int blockCode = calculateGridBlockCode(region, altitudeMap, sceneCoordinate);
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

    private static BigDecimal calculateAltitude(Region region, final IntegerCoordinate coordinate) {
        int radius = region.getRadius();
        double rst = 0D;
        switch (region.getType()) {
            case RegionConstants.REGION_TYPE_ISLAND:
                double ratio = Math.max(Math.abs(coordinate.getX()) / (double) radius,
                        Math.abs(coordinate.getY()) / (double) radius);
                if (ratio < 0.5D) {
                    rst = 0D;
                } else if (ratio < 0.9D) {
                    rst = (ratio - 0.5D) * (-1D) / 0.4D;
                } else {
                    rst = -1D;
                }
                break;
            case RegionConstants.REGION_TYPE_CHANNEL:
                // Step 1: Generate a random angle for the line passing through the origin
                double theta = Math.random() * 2 * Math.PI; // Random angle in radians
                // Step 2: Generate a random width between 1 and radius/2
                double width = random.nextDouble() * radius / 2 + 1;
                // Step 3: Calculate the distance from the point to the line
                double distance = Math.abs(coordinate.getX() * Math.cos(theta) + coordinate.getY() * Math.sin(theta));
                // Step 4: Check if the point is within the width of the line
                if (distance <= width) {
                    // Return a value proportional to how close the point is to the line center
                    rst = -1D + (distance / width); // The closer to 0, the closer to the line
                } else {
                    rst = 0D; // Outside the width range, return 0
                }
                break;
            case RegionConstants.REGION_TYPE_ISLANDS:
                int numOfIslands = random.nextInt(10);
                // 如果不在任何岛上，返回 [-1, -0.25] 区间内的随机值
                double range = 1.0 - 0.25; // 0.75
                rst = - (0.25 + random.nextDouble() * range);
                // 地图边界
                int min = -radius;
                // 遍历每个岛屿
                for (int i = 0; i < numOfIslands; i++) {
                    // 随机生成岛屿中心坐标，在 [-radius, radius] 范围内
                    int centerX = random.nextInt(radius - min + 1) + min;
                    int centerY = random.nextInt(radius - min + 1) + min;
                    // 随机生成岛屿半径：1 到 radius / numOfIslands / 5
                    int maxIslandRadius = Math.max(1, radius / numOfIslands / 5); // 确保最小为1
                    int islandRadius = random.nextInt(maxIslandRadius) + 1;
                    // 计算点到岛屿中心的距离（平方）避免开方提高性能
                    int dx = coordinate.getX() - centerX;
                    int dy = coordinate.getY() - centerY;
                    int distanceSquared = dx * dx + dy * dy;
                    int radiusSquared = islandRadius * islandRadius;
                    // 如果点在该岛屿内（含边界），则返回 0
                    if (distanceSquared <= radiusSquared) {
                        rst = 0D;
                    }
                }
                break;
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
            case RegionConstants.REGION_TYPE_EMPTY:
            default:
                break;
        }
        return BigDecimal.valueOf(rst);
    }

    private static int calculateGridBlockCode(Region region, Map<IntegerCoordinate, BigDecimal> altitudeMap,
                                              IntegerCoordinate sceneCoordinate) {
        switch (region.getType()) {
            case RegionConstants.REGION_TYPE_ISLAND:
            case RegionConstants.REGION_TYPE_CHANNEL:
            case RegionConstants.REGION_TYPE_ISLANDS:
                break;
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
                return region.getType();
            case RegionConstants.REGION_TYPE_EMPTY:
            default:
                return BlockConstants.BLOCK_CODE_BLACK;
        }
        double d = random.nextDouble();
        int blockCode;
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
        return blockCode;
    }
}
