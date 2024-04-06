package com.github.ltprc.gamepal.model.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Structure {
    private Integer undersideType; // 0-hollow 1-round 2-square
    private BigDecimal radius;
    private BigDecimal height;
}
