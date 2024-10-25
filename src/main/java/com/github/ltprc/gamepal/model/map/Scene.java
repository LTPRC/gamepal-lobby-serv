package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.block.Block;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Scene extends SceneInfo {
    private Map<String, Block> blocks;
    private int[][] gird; // terrain grid

    public Scene(Scene scene) {
        blocks = new ConcurrentHashMap<>(scene.blocks);
        gird = ArrayUtils.clone(scene.gird);
    }
}
