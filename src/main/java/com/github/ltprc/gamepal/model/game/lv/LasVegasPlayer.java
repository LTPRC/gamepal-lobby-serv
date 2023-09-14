package com.github.ltprc.gamepal.model.game.lv;

import com.github.ltprc.gamepal.model.game.Player;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LasVegasPlayer extends Player {
    private int diceNum;
    private BigDecimal money;
}
