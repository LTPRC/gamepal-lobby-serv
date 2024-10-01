package com.github.ltprc.gamepal.model.map;

import com.github.ltprc.gamepal.model.map.block.Block;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Scene extends SceneInfo {
    private List<Block> blocks;
    private List<Block> events;
    private int[][] gird; // terrain grid

    public Scene(Scene scene) {
        blocks = new CopyOnWriteArrayList<>(scene.blocks);
        events = new CopyOnWriteArrayList<>(scene.events);
        gird = ArrayUtils.clone(scene.gird);
    }
}
