package com.github.ltprc.gamepal.model.map;

import lombok.Data;

import java.util.List;

@Data
public class Scene extends SceneInfo {
    private List<Block> blocks;
}
