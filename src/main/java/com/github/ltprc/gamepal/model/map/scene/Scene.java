package com.github.ltprc.gamepal.model.map.scene;

import com.github.ltprc.gamepal.model.map.block.Block;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Scene extends SceneInfo {
    private Map<String, Block> blocks;
    private int[][] grid; // terrain grid
    private BigDecimal[][] altitude; // terrain altitude

    public Scene(Scene scene) {
        blocks = new ConcurrentHashMap<>(scene.blocks);
        grid = ArrayUtils.clone(scene.grid);
    }
}
