package com.github.ltprc.gamepal.model.map;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Scene extends SceneInfo {
    private List<Block> blocks;
    private List<Event> events;
    private int[][] gird; // terrain grid

    public Scene(Scene scene) {
        blocks = new ArrayList<>(scene.blocks);
        events = new ArrayList<>(scene.events);
        gird = ArrayUtils.clone(scene.gird);
    }
}
