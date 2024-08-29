package com.github.ltprc.gamepal.model.map;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@NoArgsConstructor
public class Scene extends SceneInfo {
    private List<Block> blocks;
    private List<Event> events;
    private int[][] gird; // terrain grid

    public Scene(Scene scene) {
        blocks = new CopyOnWriteArrayList<>(scene.blocks);
        events = new CopyOnWriteArrayList<>(scene.events);
        gird = ArrayUtils.clone(scene.gird);
    }
}
