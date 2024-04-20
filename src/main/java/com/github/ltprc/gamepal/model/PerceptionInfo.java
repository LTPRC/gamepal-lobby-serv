package com.github.ltprc.gamepal.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerceptionInfo {
    private BigDecimal distinctVisionRadius;
    private BigDecimal indistinctVisionRadius;
    private BigDecimal hearingRadius;
}
