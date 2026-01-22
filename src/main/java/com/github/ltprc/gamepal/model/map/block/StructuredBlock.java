package com.github.ltprc.gamepal.model.map.block;

import com.github.ltprc.gamepal.model.map.structure.Structure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StructuredBlock {
    private Block block;
    private Structure structure;
}
