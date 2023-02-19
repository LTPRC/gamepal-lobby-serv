package com.github.ltprc.gamepal.model.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Cash {
    private BigDecimal value;
}
