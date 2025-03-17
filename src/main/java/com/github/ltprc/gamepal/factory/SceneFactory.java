package com.github.ltprc.gamepal.factory;

import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.region.RegionInfo;
import com.github.ltprc.gamepal.model.map.scene.GravitatedStack;
import com.github.ltprc.gamepal.model.map.scene.Scene;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SceneFactory {

    private SceneFactory() {}

    public static Scene createScene(RegionInfo regionInfo, IntegerCoordinate sceneCoordinate, String name) {
        Scene scene = new Scene();
        scene.setSceneCoordinate(new IntegerCoordinate(sceneCoordinate));
        scene.setName(name);
        scene.setBlocks(new ConcurrentHashMap<>());
        scene.setGrid(new int[regionInfo.getWidth() + 1][regionInfo.getHeight() + 1]);
        scene.setGravitatedStacks(new GravitatedStack[regionInfo.getWidth()][regionInfo.getHeight()]);
        return scene;
    }

    /**
     *
     * @param region
     * @param altitudeMap
     * @param sceneCoordinate
     * @param minAltitude Threshold for polluting terrain
     * @param maxAltitude Threshold for polluting terrain
     * @param blockCode
     */
    public static void defineScene(final Region region, Map<IntegerCoordinate, BigDecimal> altitudeMap,
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
        if (null != minAltitude && altitudeMap.get(sceneCoordinate).doubleValue() < minAltitude) {
            return;
        }
        if (null != maxAltitude && altitudeMap.get(sceneCoordinate).doubleValue() > maxAltitude) {
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
}
