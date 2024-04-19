package com.github.ltprc.gamepal.model.map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SceneInfo {
    private String name;
    private IntegerCoordinate sceneCoordinate;

    public SceneInfo(SceneInfo sceneInfo) {
        name = sceneInfo.name;
        sceneCoordinate = new IntegerCoordinate(sceneInfo.sceneCoordinate);
    }
}
