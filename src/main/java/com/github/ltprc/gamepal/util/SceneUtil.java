package com.github.ltprc.gamepal.util;

import com.github.ltprc.gamepal.model.map.coordinate.IntegerCoordinate;
import com.github.ltprc.gamepal.model.map.region.Region;
import com.github.ltprc.gamepal.model.map.scene.GravitatedStack;
import com.github.ltprc.gamepal.model.map.scene.Scene;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class SceneUtil {

    private static final Random random = new Random();

    private SceneUtil() {}

    public static void initiateSceneGravitatedStacks(Region region, Scene scene) {
        for (int i = 0; i < scene.getGravitatedStacks().length; i++) {
            for (int j = 0; j < scene.getGravitatedStacks()[i].length; j++) {
                BigDecimal[][] altitudes = new BigDecimal[3][3];
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        Scene currentScene = region.getScenes().getOrDefault(new IntegerCoordinate(i + (k - 1), j + (l - 1)), scene);
                        altitudes[k][l] = region.getAltitude().add(region.getAltitudeMap()
                                .getOrDefault(currentScene.getSceneCoordinate(), BigDecimal.ZERO));
                    }
                }
                scene.getGravitatedStacks()[i][j] = new GravitatedStack(altitudes[1][1].add(
                        BigDecimal.valueOf((random.nextDouble() - 0.5D) * 0.2D).setScale(2, RoundingMode.HALF_UP)));
            }
        }
    }
}
