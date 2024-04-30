package com.github.ltprc.gamepal.model.creature;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerceptionInfo {
    private BigDecimal distinctVisionRadius;
    private BigDecimal indistinctVisionRadius;
    private BigDecimal distinctVisionAngle;
    private BigDecimal indistinctVisionAngle;
    private BigDecimal distinctHearingRadius;
    private BigDecimal indistinctHearingRadius;
}
