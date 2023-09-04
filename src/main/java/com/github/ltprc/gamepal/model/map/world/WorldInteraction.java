package com.github.ltprc.gamepal.model.map.world;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class WorldInteraction extends WorldBlock {
    private List<Integer> interactions;
}
