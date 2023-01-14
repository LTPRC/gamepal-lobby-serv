package com.github.ltprc.gamepal.model.map;

import lombok.Data;

import javax.annotation.sql.DataSourceDefinition;
import java.math.BigDecimal;


@Data
public class Coordinate {
    private BigDecimal x;
    private BigDecimal y;
}
