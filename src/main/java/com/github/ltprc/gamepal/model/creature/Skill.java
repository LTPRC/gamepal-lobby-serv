package com.github.ltprc.gamepal.model.creature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    private int skillCode;
    private int skillMode;
    private int frame;
    private int frameMax;
    private int skillType;
    private BigDecimal range;
}
