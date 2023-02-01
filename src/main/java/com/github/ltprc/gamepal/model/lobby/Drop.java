package com.github.ltprc.gamepal.model.lobby;

import com.github.ltprc.gamepal.model.map.SceneCoordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drop extends SceneCoordinate {

    private String itemNo;
    private int amount;
}
